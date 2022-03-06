package site.metacoding.reflect.controller;

import site.metacoding.reflect.anno.RequestMapping;
import site.metacoding.reflect.controller.dto.JoinDto;
import site.metacoding.reflect.controller.dto.LoginDto;
import site.metacoding.reflect.model.User;

public class UserController {

	// 가독성 위해 어노테이션을 사용하자.

	@RequestMapping("/join")
	public String join(JoinDto dto) {
		System.out.println("join 함수 호출됨");
		System.out.println(dto);
		return "/";
	}

	@RequestMapping("/login")
	public String login(LoginDto dto) {
		System.out.println("login 함수 호출됨");
		System.out.println(dto);
		return "/";
	}

	@RequestMapping("/list")
	public String user(User user) {
		System.out.println("list 함수 호출됨");
		System.out.println(user);
		return "/";
	}

	@RequestMapping("/detail")
	public String detail(User user) {
		System.out.println("detail 함수 호출됨");
		return "/";
	}
}
