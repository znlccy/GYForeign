package com.gy.services;

import java.util.List;

import com.gy.dao.GoodsDao;
import com.gy.model.Goods;

/**
 * @author Chencongye
 * @version 0.0.1
 * @introduce 这是商品服务层接口
 * @date 2017.9.12
 */

public interface GoodsService {
	
	/**
	 * 实现商品单个查询功能
	 * @return true or false
	 */
	public Goods query(int goodsid);
	
	/**
	 * 实现查询所有商品
	 * @return List集合
	 */
	public List<Goods> queryAll();
	
	/**
	 * 实现商品添加功能
	 * @return true or false
	 */
	public boolean save(Goods goods);
	
	/**
	 * 增加一些商品
	 * @return true or false
	 */
	public boolean saveAll(Goods[] goods);
	
	/**
	 * 实现删除一个商品
	 * @return true or false
	 */
	public boolean delete(int goodsid);
	
	/**
	 * 实现删除某些商品
	 * @return true or false
	 */
	public boolean deleteAll(Goods[] goods);
	
	/**
	 * 更新一个商品
	 * @return
	 */
	public boolean modify(int goodsid);
		
	/**
	 * 更新所有商品
	 * @return true or false
	 */
	public boolean updateAll(Goods[] goods);
}
