package com.pinyougou.sellergoods.service;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import entity.PageResult;

import java.util.List;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);
    /**
     * 批量虚拟删除
     * @param ids
     */
    public void isDelete(Long[] ids);

    /**
     * 批量修改状态
     * @param ids
     * @param status
     */
	public void updateStatus(Long[] ids,String status);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum,int pageSize);

    /**
     * 通过goodid和商品状态查询商品表
     * @param goodsIds
     * @param status
     * @return
     */
    public  List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status );
	

	
}
