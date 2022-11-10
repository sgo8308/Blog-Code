package com.db.concurrencyproblem;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    private final NamedLock namedLock;

    public void subtractStockOptimistic(int productId, int quantity){
        int updatedCount = 0;
        while (updatedCount == 0){
            Product product = productRepository.findById(productId);
            product.subtractStock(quantity);
            updatedCount = productRepository.updateStockOptimistic(product);

//            if (updatedCount <= 0) { //업데이트에 실패한 경우 50ms 대기
//                try {
//                    Thread.sleep(25);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }

    @Transactional
    public void subtractStockPessimistic(int productId, int quantity){
        Product product = productRepository.findByIdForUpdate(productId);
        product.subtractStock(quantity);
        productRepository.updateStock(product);
    }

    @Transactional
    public void subtractStockAtomic(int productId, int quantity){
        productRepository.updateStockAtomic(productId, quantity);
    }

    public void subtractStockNamedLock(int productId, int quantity){
        String lockName = "subtract_stock_"+productId;
        int timeout = 3;
        namedLock.executeWithLock(lockName, timeout, () -> subtractStock(productId, quantity));
    }
    @Transactional
    public int subtractStock(int productId, int quantity){
        Product product = productRepository.findById(productId);
        product.subtractStock(quantity);
        int updatedCount = productRepository.updateStock(product);
        return updatedCount;
    }

    private final RedissonClient redissonClient;

    public void subtractStockRedisson(int productId, int quantity){

        RLock lock = redissonClient.getLock("product_stock_id_1");// 1

        try {
            boolean canLock = lock.tryLock(5 , 1, TimeUnit.SECONDS); // 2

            if(!canLock){
                System.out.println("락 획득 실패");
                return;
            }

            subtractStock(productId, quantity);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally{
            lock.unlock(); // 3
        }
    }



    public void insertProduct(Product product){
        productRepository.insert(product);
    }


    public void deleteAll() {
        productRepository.deleteAll();
    }
}
