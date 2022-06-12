package com.yijianguanzhu.transfer.server.message;

import com.yijianguanzhu.transfer.common.message.Message;
import com.yijianguanzhu.transfer.server.enums.CmdEnum;
import com.yijianguanzhu.transfer.server.enums.CodeEnum;
import lombok.*;

/**
 * @author yijianguanzhu 2022年05月17日
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppMessage implements Message {

	// 命令
	private CmdEnum cmd;

	// 时间戳
	@Builder.Default
	private long timestamp = System.currentTimeMillis();

	// 状态
	@Builder.Default
	private String status = "ok";

	// 请求体
	private String body;

	// 代码
	private CodeEnum code;

	// 错误描述
	private String errMsg;

	private String msg;

	// 帮助
	@Builder.Default
	private String help = "Please make sure that all required fields are filled with acceptable and accurare data.";

	public String getErrMsg() {
		return cmd == CmdEnum.ERROR ? code.getMsg() : null;
	}

	public String getHelp() {
		return cmd == CmdEnum.ERROR ? help : null;
	}
}
