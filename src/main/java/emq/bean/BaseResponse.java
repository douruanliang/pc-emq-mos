package emq.bean;

import java.io.Serializable;

/**
 * File description.
 *
 * @author dourl
 * @date 2022/3/17
 */
public class BaseResponse implements Serializable{
    public String message;
    public int code = 0;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public BaseResponse(int code,String message) {
		super();
		this.message = message;
		this.code = code;
	}
    
    
    
}
