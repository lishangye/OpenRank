package com.openrank.openrank.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AiAssistant {

    @SystemMessage("你是开源仓库助手，返回开源仓库的具体信息。")
    String chat(@UserMessage String message);
}
