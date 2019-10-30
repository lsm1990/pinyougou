package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class ItemPageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long [] goodsIds= (Long[]) objectMessage.getObject();
            System.out.println("接收到消息：" + goodsIds);
            for(Long goodsId:goodsIds){
                boolean b = itemPageService.genItemHtml(goodsId);
                System.out.println("网页生成结果：" + b);
            }

        } catch (JMSException e) {

            e.printStackTrace();
        }

    }

}
