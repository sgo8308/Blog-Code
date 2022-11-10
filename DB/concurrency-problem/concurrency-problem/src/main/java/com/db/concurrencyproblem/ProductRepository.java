package com.db.concurrencyproblem;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ProductRepository {


    Product findById(long id);

    Product findByIdForUpdate(long id);

    int updateStockOptimistic(Product product);

    int updateStock(Product product);

    void updateStockAtomic(@Param("id") int id,@Param("quantity") int quantity);

    void insert(Product product);

    void deleteAll();
}
