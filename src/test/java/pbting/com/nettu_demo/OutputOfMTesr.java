package pbting.com.nettu_demo;

import java.lang.reflect.Field;

public class OutputOfMTesr {

	static int count = 0 ;
	public static void main(String[] args) throws Exception, Exception {
		Persson per = new Persson();
		per.setAge(1);
		per.setName("pbt");
		Field[] fields = Persson.class.getDeclaredFields();
		System.out.println(fields.length);
		for (int i = 0; i < fields.length; i++) {
			System.out.println(fields[i].get(per));
		}
	}
	
	public static void test(int i){
		int[] e = new int[i++];
		test(i);
		System.out.println(count++);
	}
}


class Persson{
	
	int age ;
	String name;
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}