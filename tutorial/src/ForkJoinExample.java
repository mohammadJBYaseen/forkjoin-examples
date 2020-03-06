import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinExample {

    public static void main(String[] argc){
        List<BigInteger> collect = IntStream.range(1, 18).parallel().mapToObj(BigInteger::valueOf).collect(Collectors.toList());
        ForkJoinPool.commonPool().invoke(new CounterFactorialTask(null,collect));
    }


    public static class FactorialTask extends RecursiveAction {

        private static final int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;

        private FactorialTask(List<BigInteger> integerList) {
            this.integerList = integerList;
        }

        @Override
        protected void compute() {
            System.out.println("sub list size "+integerList.size());
            if (integerList.size() <= SEQUENTIAL_THRESHOLD) {
                showFactorials();
            } else {
                //splitting
                int middle = integerList.size() / 2;
                List<BigInteger> newList = integerList.subList(middle, integerList.size());
                integerList = integerList.subList(0, middle);

                FactorialTask task = new FactorialTask(newList);
                //fork() method returns immediately but spawn a new thread for the task
                task.fork();
                this.compute();
            }
        }

        private void showFactorials() {
            BigInteger sum = BigInteger.ZERO;
            for (BigInteger i : integerList) {
                BigInteger factorial = factorial(i);
                System.out.println("factorial of "+i.toString()+" is "+factorial.toString());
                sum = sum.add(factorial);
            }
            System.out.println("factorial is "+sum.toString());
        }

        private BigInteger factorial(BigInteger x){
            if(x.equals(BigInteger.ONE) || x.equals(BigInteger.ZERO)){
                return BigInteger.ONE;
            }
            return x.multiply(factorial(x.add(BigInteger.ONE.negate())));
        }
    }

    public static class CounterFactorialTask extends CountedCompleter<Void>{

        private static int SEQUENTIAL_THRESHOLD = 5;
        private List<BigInteger> integerList;

        private CounterFactorialTask (CountedCompleter<Void> parent,
                               List<BigInteger> integerList) {
            super(parent);
            this.integerList = integerList;
        }


        @Override
        public void compute() {
            System.out.println("sub list size "+integerList.size());
            if (integerList.size() <= SEQUENTIAL_THRESHOLD) {
                showFactorials();
                tryComplete();
            } else {
                //splitting
                int middle = integerList.size() / 2;
                List<BigInteger> newList = integerList.subList(middle, integerList.size());
                integerList = integerList.subList(0, middle);

                CounterFactorialTask task = new CounterFactorialTask(this,newList);
                addToPendingCount(1);
                //fork() method returns immediately but spawn a new thread for the task

                task.fork();
                this.compute();
            }
        }


        private void showFactorials() {
            BigInteger sum = BigInteger.ZERO;
            for (BigInteger i : integerList) {
                BigInteger factorial = factorial(i);
                System.out.println("factorial of "+i.toString()+" is "+factorial.toString());
                sum = sum.add(factorial);
            }
            System.out.println("factorial is "+sum.toString());
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if(this == caller){
                System.out.println("pending tasks is "+ getPendingCount());
            }else{
                addToPendingCount(-1);
                System.out.println("pending tasks is "+ getPendingCount());
            }
        }


        private BigInteger factorial(BigInteger x){
            if(x.equals(BigInteger.ONE) || x.equals(BigInteger.ZERO)){
                return BigInteger.ONE;
            }
            return x.multiply(factorial(x.add(BigInteger.ONE.negate())));
        }
    }
}
