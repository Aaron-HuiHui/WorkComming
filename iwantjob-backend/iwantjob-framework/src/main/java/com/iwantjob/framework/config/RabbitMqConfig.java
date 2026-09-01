package com.iwantjob.framework.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 事件总线拓扑
 * <p>
 * 交换机 iwantjob.event（topic，durable）：
 * <ul>
 *   <li>event.badge.trigger → iwantjob.badge.trigger 队列（核心服务 badge 模块消费，铸造徽章）</li>
 *   <li>event.point.change  → iwantjob.point.change  队列（核心服务 user 模块消费，增减互助积分）</li>
 * </ul>
 * 发布侧：业务代码仍走 ApplicationEventPublisher，由 MqEventRelay 在事务提交后转发到本交换机，
 * 职位服务（job-server）等跨进程发布方也可直接向本交换机投递，消费方无感。
 * 队列声明幂等：核心/职位服务启动时都会通过 RabbitAdmin 声明，参数一致。
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "iwantjob.event";
    public static final String QUEUE_BADGE = "iwantjob.badge.trigger";
    public static final String QUEUE_POINT = "iwantjob.point.change";
    public static final String RK_BADGE = "event.badge.trigger";
    public static final String RK_POINT = "event.point.change";

    @Bean
    public TopicExchange eventExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue badgeQueue() {
        return QueueBuilder.durable(QUEUE_BADGE).build();
    }

    @Bean
    public Queue pointQueue() {
        return QueueBuilder.durable(QUEUE_POINT).build();
    }

    @Bean
    public Binding badgeBinding() {
        return BindingBuilder.bind(badgeQueue()).to(eventExchange()).with(RK_BADGE);
    }

    @Bean
    public Binding pointBinding() {
        return BindingBuilder.bind(pointQueue()).to(eventExchange()).with(RK_POINT);
    }

    /**
     * JSON 消息转换器（默认信任所有包，收发双端使用统一 DTO）。
     * RabbitTemplate 与 @RabbitListener 容器工厂均由 Boot 自动装配引用此 Bean。
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
