import com.sun.istack.internal.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinRecursiveActionWithSplitor {

    public static void main(String [] argc){
        ForkJoinPool.commonPool().invoke(new RecursiveFactorialActionSplitor(IntStream.range(1, 10000)
                .mapToObj(BigInteger::valueOf).spliterator()));
    }

    public static  class RecursiveFactorialActionSplitor extends RecursiveAction{

        private Spliterator<BigInteger> spliterator;

        public RecursiveFactorialActionSplitor(@NotNull Spliterator<BigInteger> values){
            spliterator = values;
        }

        @Override
        protected void compute() {
                Spliterator<BigInteger> split = spliterator.trySplit();
                if(split != null) {
                    new RecursiveFactorialActionSplitor(split).fork();
                }else{
                    spliterator.forEachRemaining(item-> System.out.println(factory(item)));
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
