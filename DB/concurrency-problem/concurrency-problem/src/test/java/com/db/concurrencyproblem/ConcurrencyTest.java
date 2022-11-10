package com.db.concurrencyproblem;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;

//@Transactional
@SpringBootTest(classes = ConcurrencyProblemApplication.class)
public class ConcurrencyTest {


    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    void test() throws InterruptedException {
        for (int i = 0; i < 10; i++) { // 10번 테스트 진행
            List<Integer> times = new ArrayList<>();

            // 100개의 재고를 가진 상품 생성
            Product product = Product.builder().stock(100).name("상품").build();
            productService.insertProduct(product);

            // 100개의 쓰레드로 동시에 재고를 1씩 감소하는 테스트 진행
            long start = System.currentTimeMillis();
            executeWithThreads(times, () -> productService.subtractStockNamedLock(product.getId(), 1));
            long end = System.currentTimeMillis();

            int stock = productRepository.findById(product.getId()).getStock();

            assertThat(stock).isZero(); // 정확히 100개가 차감되었는지 체크

            productService.deleteAll();

            System.out.println("평균 메소드 수행 시간 :" + times.stream().mapToInt(Integer::intValue).average().getAsDouble() +"ms");
            System.out.println("100개의 쓰레드로 100개의 재고를 1씩 감소하는데 걸린 총 시간 : " + (end - start) + "ms");
        }
    }

    void executeWithThreads(List<Integer> times, Runnable runnable) throws InterruptedException {
            //Thread 100개 생성 후 차감 진행
            List<Thread> threads = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                Thread thread = new Thread(() -> timeCheck(times, runnable));
                threads.add(thread);
                thread.start();
            }

            //모든 쓰레드가 끝나기를 기다리기
            for (Thread thread : threads) {
                thread.join();
            }
    }

    void timeCheck(List<Integer> times, Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        long end = System.currentTimeMillis();
        times.add((int) (end - start));
    }
}
