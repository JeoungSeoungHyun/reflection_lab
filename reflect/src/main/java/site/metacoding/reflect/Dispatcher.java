package site.metacoding.reflect;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import site.metacoding.reflect.anno.RequestMapping;
import site.metacoding.reflect.controller.UserController;

public class Dispatcher implements Filter {

	private boolean isMatching = false;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String endPoint = request.getRequestURI();

//	System.out.println("컨텍스트 패스 : " + request.getContextPath());
		System.out.println("엔드 포인트 : " + endPoint);
//	System.out.println("전체주소 : " + request.getRequestURL());

		UserController userController = new UserController();

		// 1.리플렉션이 필요한 이유
		// 메서드 직접 분기해보기 => 새로 추가될 때마다 유지보수하기 힘듬
//	if (endPoint.equals("/join")) {
//		userController.join();
//	} else if (endPoint.equals("/login")) {
//		userController.login();
//	}

		// 리플렉션!!!!! -> 메서드를 런타임시 찾아내서 실행
		// 변수가 들고 있는 메서드를 알수있다.
		// declaredMethods는 그 클래스의 메서드만 Methods는 상속받는 메서드까지 알수있다.
		Method[] methods = userController.getClass().getDeclaredMethods();

//	for (Method method : methods) {
////		System.out.println(method.getName());
//		if (endPoint.equals("/" + method.getName())) {
//			try {
		// invoke메서드(사용할 객체,인자)
//				method.invoke(userController);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

		for (Method method : methods) { // 4바퀴(join, login, user, hello)
			Annotation annotation = method.getDeclaredAnnotation(RequestMapping.class);
			// 다운캐스팅이 필요하게 된다.(그래야 RequestMapping 어노테이션에 있는 추상메서드 사용가능)
			RequestMapping requestMapping = (RequestMapping) annotation;
//			System.out.println(requestMapping.value());

			if (requestMapping.value().equals(endPoint)) {
				isMatching = true;
				try {
					Parameter[] params = method.getParameters();
					String path;

//					System.out.println("params[0].getType() : " + params[0].getType());					

					if (params.length != 0) {
						// 실행시 동작하기 때문에 어떤 메서드가 실행되어 어떤 parameter가 들어올지 모르므로
						// Object 타입으로 받아주어야 한다.
						// 입력데이터를 받는 오브젝트(Dto)를 리플렉션해서 set함수 호출
						Object dtoinstance = params[0].getType().getConstructor(null).newInstance();
//						String username = request.getParameter("username");
//						String password = request.getParameter("password");
//						System.out.println("username : " + username);
//						System.out.println("password : " + password);

						setData(dtoinstance, request);

						path = (String) method.invoke(userController, dtoinstance);
					} else {
						// invoke리턴 타입은 Object이므로 다운캐스팅
						path = (String) method.invoke(userController);
					}

					System.out.println("path : " + path);
					// 리퀘스트 디스패쳐는 web.xml에 의해 필터링 되지 않아서 경로가 찾아진다
					RequestDispatcher dis = req.getRequestDispatcher(path);
					dis.forward(request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break; // 함수 실행시 종료되도록
			}
		}
		if (isMatching == false) {
			try {
				response.setContentType("text/html; charset=utf-8");
				PrintWriter out = response.getWriter();
				out.println("잘못된 주소요청입니다.");
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private <T> void setData(T instance, HttpServletRequest request) {
		Enumeration<String> keys = request.getParameterNames(); // username,password
		// keys를 변형해서 set메서드와 같이 만들어준 다음 위에서 생성한 객체의 값을 정해준다.
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String methodKey = keyToMethodKey(key);
//			System.out.println("methodKey : " + methodKey);

			Method[] methods = instance.getClass().getDeclaredMethods();

			for (Method method : methods) {
				if (method.getName().equals(methodKey)) {
					try {
						if (key.equals("id")) {
							method.invoke(instance, Integer.parseInt(request.getParameter(key)));
						} else {
							method.invoke(instance, request.getParameter(key));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	private String keyToMethodKey(String key) {
		return "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
	}
}
