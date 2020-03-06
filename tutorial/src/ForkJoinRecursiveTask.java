import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinRecursiveTask {

    public static void main(String [] argc){
        ForkJoinTask<BigInteger> submit = ForkJoinPool.commonPool().submit(new CompletableFactorialTask(IntStream.range(1, 6)
                .mapToObj(BigInteger::valueOf).collect(Collectors.toList())));
        BigInteger join = submit.join();
        System.out.println(join);

        BigInteger invoke = ForkJoinPool.commonPool().invoke(new CompletableFactorialTask(IntStream.range(1, 6)
                .mapToObj(BigInteger::valueOf).collect(Collectors.toList())));
        System.out.println(invoke);
    }

    public static class CompletableFactorialTask extends RecursiveTask<BigInteger>{

        private List<BigInteger> integerList;

        public CompletableFactorialTask(List<BigInteger> integers){
            this.integerList = integers;
        }

        @Override
        protected BigInteger compute() {
           if(integerList.size() < 5){
               return factorialSum(integerList);
           }else{
               int middle = integerList.size() / 2;
               CompletableFactorialTask leftTask = new CompletableFactorialTask(integerList.subList(0, middle));
               CompletableFactorialTask rightTask = new CompletableFactorialTask(integerList.subList(middle, integerList.size()));
               ForkJoinTask<BigInteger> right = leftTask.fork();
               ForkJoinTask<BigInteger> left = rightTask.fork();
               BigInteger sum = BigInteger.ZERO;
               sum = sum.add(right.join());
               sum = sum.add(left.join());
               return sum;
           }
        }

        private BigInteger factorialSum(List<BigInteger> integerList) {
            return integerList.stream().map(this::factory).reduce(BigInteger::add).orElseGet(()->BigInteger.ZERO);
        }

        private BigInteger factory(BigInteger bigInteger) {
            if(bigInteger.compareTo(BigInteger.ONE)<=0){
                return BigInteger.ONE;
            }
            return bigInteger.multiply(factory(bigInteger.add(BigInteger.ONE.negate())));
        }


    }

}
