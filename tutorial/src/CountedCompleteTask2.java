import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

public class CountedCompleteTask2 {

    public static void main (String[] args) {
        List<BigInteger> list = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            list.add(new BigInteger(Integer.toString(i)));
        }

        BigInteger sum = ForkJoinPool.commonPool().
                invoke(new FactorialTask(null,
                        list));
        System.out.println("Sum of the factorials = " + sum);
    }


    private static class FactorialTask extends CountedCompleter<BigInteger> {
        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;
        private AtomicReference<BigInteger> result;

        private FactorialTask (CountedCompleter<BigInteger> parent,
                               List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
            this.result = new AtomicReference<>(new BigInteger("0"));
        }

        private FactorialTask (CountedCompleter<BigInteger> parent,
                               AtomicReference<BigInteger> result,
                               List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
            this.result = result;
        }

        @Override
        public BigInteger getRawResult () {
            return result.get();
        }

        @Override
        public void compute () {

            //this example creates all sub-tasks in this while loop
            while (integerList.size() > SEQUENTIAL_THRESHOLD) {

                //end of the list containing SEQUENTIAL_THRESHOLD items.
                List<BigInteger> newTaskList = integerList.subList(integerList.size() -
                        SEQUENTIAL_THRESHOLD, integerList.size());

                //remaining list
                integerList = integerList.subList(0, integerList.size() -
                        SEQUENTIAL_THRESHOLD);

                addToPendingCount(1);
                FactorialTask task = new FactorialTask(this, result, newTaskList);
                task.fork();
            }
            //find sum of factorials of the remaining this.integerList
            sumFactorials();
            propagateCompletion();
            System.out.println(getPendingCount());
        }


        private void addFactorialToResult (BigInteger factorial) {
            result.getAndAccumulate(factorial, (b1, b2) -> b1.add(b2));
        }

        private void sumFactorials () {

            for (BigInteger i : integerList) {
                addFactorialToResult(factory(i));
            }
        }

        static BigInteger factory(BigInteger bigInteger) {
            if(bigInteger.compareTo(BigInteger.ONE)<=0){
                return BigInteger.ONE;
            }
            return bigInteger.multiply(factory(bigInteger.add(BigInteger.ONE.negate())));
        }
    }
}
