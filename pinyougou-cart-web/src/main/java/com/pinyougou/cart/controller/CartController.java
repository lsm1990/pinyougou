package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制类
 * @author lsm
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 由cookie中查找购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){


        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        final String user ="anonymousUser";


        String cartListString = CookieUtil.getCookieValue(request,"cartList","UTF-8");
        if(cartListString==null || "".equals(cartListString) ){

            cartListString="[]";
        }
        List<Cart> cartListCookie = JSON.parseArray(cartListString,Cart.class);
        //未登录
        if(user.equals(username)){
            return cartListCookie;
        }else{
            List<Cart> cartListRedis = cartService.findCartListFromRedis(username);
            //合并购物车
            if(cartListCookie.size()>0){
               cartListRedis = cartService.mergeCartList(cartListCookie,cartListRedis);
               //清除Cookie中的数据
                CookieUtil.deleteCookie(request,response,"cartList");
                //合并后存入redis
                cartService.addCartListToRedis(username,cartListRedis);
            }

            return  cartListRedis;

        }

    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){

        //当前登录人账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+name);
        try{
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            final String user = "anonymousUser";
            if(user.equals(name)){
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向cookie存入数据");
            }else {
                cartService.addCartListToRedis(name,cartList);

            }

            return new Result(true,"添加成功");

        }catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());

        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }
}
