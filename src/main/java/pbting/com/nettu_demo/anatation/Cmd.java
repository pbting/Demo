package pbting.com.nettu_demo.anatation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface Cmd {

	public String code() default "403";
	
	public MethodType metohd() default MethodType.GET ;
	
	/**
	 * 定义方法的美剧
	 */
	public enum MethodType {GET,POST,PUT,DELETE};

	public final static String CODE_1 = "404";
}
