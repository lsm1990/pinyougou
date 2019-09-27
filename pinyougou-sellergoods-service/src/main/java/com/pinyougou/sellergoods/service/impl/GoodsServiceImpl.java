package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
    private TbItemMapper itemMapper;
	@Autowired
    private TbItemCatMapper itemCatMapper;
	@Autowired
    private TbBrandMapper brandMapper;
	@Autowired
    private  TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		
		goods.getGoods().setAuditStatus("0");//默认未申请状态
		
		goodsMapper.insert(goods.getGoods());
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置商品描述id
		
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
        //插入SKU列表数据
        setItemList(goods);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改商品信息
        //设置未申请状态:如果是经过修改的商品，需要重新设置状态
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        //修改商品扩展信息
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //先修改SKU信息
        //先删除，再保存
        //删除SKU信息
        TbItemExample itemExample = new TbItemExample();
        com.pinyougou.pojo.TbItemExample.Criteria criteria = itemExample.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(itemExample);
        //再保存SKU
        setItemList(goods);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
	    Goods goods = new Goods();
	    //商品基本信息
	    TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
	    goods.setGoods(tbGoods);
	    //商品描述
	    TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
	    goods.setGoodsDesc(tbGoodsDesc);

	    //查询SKU表
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);
        goods.setItemList(itemList);


	    return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
    /**
     * 批量虚拟删除
     */
    @Override
    public void isDelete(Long[] ids) {
        for(Long id:ids){
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //isDelete 1代表被移除，null正常
            goods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(goods);

        }
    }

    /**
     * 批量修改状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for(Long id : ids){
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(goods);
        }
    }


    @Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
        criteria.andIsDeleteIsNull();//非删除状态
		
		if(goods!=null){			
		    if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	//插入SKU表
    private void setItemList(Goods goods){
	    //启用规格
	    if("1".equals(goods.getGoods().getIsEnableSpec())){
	        //保存SKU表
	        for(TbItem item : goods.getItemList()){
	           String title = goods.getGoods().getGoodsName();
                Map<String,String> map = JSON.parseObject(item.getSpec(),Map.class);
                for(String key : map.keySet()){
                    title+=" "+map.get(key);
                }
                item.setTitle(title);
                setValue(goods,item);
                itemMapper.insert(item);
            }
        }else{
            // 没有启用规格
            TbItem item = new TbItem();

            item.setTitle(goods.getGoods().getGoodsName());

            item.setPrice(goods.getGoods().getPrice());

            item.setNum(999);

            item.setStatus("0");

            item.setIsDefault("1");
            item.setSpec("{}");
            //item.setSpec(new HashMap());
            setValue(goods,item);
            itemMapper.insert(item);
        }
    }

    private void setValue(Goods goods,TbItem item){
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
        if(imageList.size()>0){
            item.setImage((String)imageList.get(0).get("url"));
        }

        // 保存三级分类的ID:
        item.setCategoryid(goods.getGoods().getCategory3Id());
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());

        // 设置商品ID
        item.setGoodsId(goods.getGoods().getId());
        item.setSellerId(goods.getGoods().getSellerId());

        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());

        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
    }


}
