import com.sun.istack.internal.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinRecursiveAction {

    public static void main(String [] argc){
        ForkJoinPool.commonPool().invoke(new RecursiveFactorialAction(IntStream.range(1, 6)
                .mapToObj(BigInteger::valueOf).collect(Collectors.toList())));

    }


    public static class RecursiveFactorialAction extends RecursiveAction{

        private List<BigInteger> numbers;

        public RecursiveFactorialAction(@NotNull List<BigInteger> values){
            numbers = values;
        }

        @Override
        protected void compute() {
            if(numbers.size() < 5){
                System.out.println(factorialSum(numbers));
            }else{
                int middle = numbers.size() / 2;
                ForkJoinRecursiveTask.CompletableFactorialTask leftTask = new ForkJoinRecursiveTask.CompletableFactorialTask(numbers.subList(0, middle));
                numbers = numbers.subList(middle,numbers.size());
                leftTask.fork();
                this.compute();
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
