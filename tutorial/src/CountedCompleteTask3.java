import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CountedCompleteTask3 {

    public static void main (String[] args) {
        List<BigInteger> list = new ArrayList<>();
        for (int i = 1; i < 1000; i++) {
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
            setPendingCount(0);
        }

        @Override
        public BigInteger getRawResult () {
            return result.get();
        }

        @Override
        public void compute () {

            //this example creates all sub-tasks in this while loop
            if(integerList.size() >5){
                AtomicInteger index = new AtomicInteger(0);
                Map<Integer, List<BigInteger>> chunks = integerList.stream().collect(Collectors.groupingBy(item -> index.getAndIncrement() / 5, Collectors.toList()));
                Stream<FactorialTask> factorialTaskStream = chunks.values().stream().map(list -> new FactorialTask(this, result, list));
                addToPendingCount(chunks.size());
                System.out.println("pending tasks "+ getPendingCount());
                factorialTaskStream.forEach(FactorialTask::fork);
                System.out.println("pending tasks "+ getPendingCount());
            } else {
                sumFactorials();
            }
            propagateCompletion();
            System.out.println("pending tasks "+ getPendingCount());
        }


        private void addFactorialToResult (BigInteger factorial) {
            result.getAndAccumulate(factorial, BigInteger::add);
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
