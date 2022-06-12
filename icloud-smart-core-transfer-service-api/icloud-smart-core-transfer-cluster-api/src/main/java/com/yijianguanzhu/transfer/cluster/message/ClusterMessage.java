package com.yijianguanzhu.transfer.cluster.message;

import com.yijianguanzhu.transfer.cluster.enums.ClusterCmdEnum;
import com.yijianguanzhu.transfer.common.message.Message;
import lombok.*;

/**
 * @author yijianguanzhu 2022年05月20日
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClusterMessage implements Message {

	// 实例id
	private String instanceId;

	// 命令
	private ClusterCmdEnum clusterCmdEnum;

	// 消息
	private String msg;

	// 账号标识
	private Long userId;

	// 账号名
	private String username;
}
