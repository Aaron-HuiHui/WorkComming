package com.iwantjob.ai;

/**
 * 流式对话回调接口。
 * 业务方在调用 chatStream 时实现，由 AI 模块在收到增量、完成、异常时回调。
 */
public interface StreamCallback {

    /**
     * 每次收到增量文本时触发
     */
    void onMessage(String delta);

    /**
     * 流式结束时触发，返回完整文本
     */
    void onComplete(String full);

    /**
     * 出现异常时触发
     */
    void onError(Throwable e);
}
