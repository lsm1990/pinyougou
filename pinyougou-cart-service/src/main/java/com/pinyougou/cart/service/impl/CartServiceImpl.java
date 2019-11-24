package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lsm
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return cartList
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!new String(String.valueOf(1)).equals(item.getStatus())){
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
       String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);


        //4.如果购物车列表中不存在该商家的购物车
        if(cart==null){

            //4.1 新建购物车对象
           cart = new Cart();
           cart.setSellerId(sellerId);
           cart.setSellerName(item.getSeller());
           TbOrderItem orderItem = createOderItem(item,num);
           List<TbOrderItem> orderItemList = new ArrayList<>();
           orderItemList.add(orderItem);
           cart.setOrderItemlist(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else{
            //5.如果购物车列表中存在该商家的购物车

            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemlist(),itemId);
            if(orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem = createOderItem(item , num);
                cart.getOrderItemlist().add(orderItem);

            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(BigDecimal.valueOf(orderItem.getNum()*orderItem.getPrice().doubleValue()));
                if(orderItem.getNum()<=0){
                    cart.getOrderItemlist().remove(orderItem);
                }else if(cart.getOrderItemlist().size()==0){
                    cartList.remove(cart);
                }
            }

        }

        return cartList;
    }
    /**
     * 根据商家ID查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */

    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){

        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据商品明细ID查询
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for(TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOderItem(TbItem item,Integer num){
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        return orderItem;
    }
}

