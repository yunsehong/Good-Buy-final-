package com.kh.goodbuy.member.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kh.goodbuy.common.model.service.ReportService;
import com.kh.goodbuy.common.model.vo.Alarm;
import com.kh.goodbuy.common.model.vo.Messenger;
import com.kh.goodbuy.member.model.service.MemberService;
import com.kh.goodbuy.member.model.vo.Member;
import com.kh.goodbuy.member.model.vo.MyTown;
import com.kh.goodbuy.member.model.vo.NaverLoginBO;
import com.kh.goodbuy.town.model.service.TownService;
import com.kh.goodbuy.town.model.vo.Town;

import net.sf.json.JSONObject;


@Controller
@RequestMapping("/member")
@SessionAttributes({ "loginUser", "msg", "townInfo","mtlist", "writeActive", "kakaoMember"})
public class MemberController {

	@Autowired
	private MemberService mService;
	@Autowired
	private TownService tService;
	@Autowired
	private ReportService reService;
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;

	KakaoAPI kakaoApi = new KakaoAPI();
	
	/* NaverLoginBO */
    private NaverLoginBO naverLoginBO;
    private String apiResult = null;
    
    @Autowired
    private void setNaverLoginBO(NaverLoginBO naverLoginBO) {
        this.naverLoginBO = naverLoginBO;
    }

	// 3_2. 일반 로그인 컨트롤러 (DB select)
	@PostMapping("/login") // 일반 로그인 post 방식
	public String userLogin(@ModelAttribute Member m, Model model,HttpServletRequest request) {
//		   System.out.println("m" + m);
		// 신고 날짜+15 < 오늘 날짜 
		int checkDate = reService.updateReportedDate(m.getUser_id());
		
		System.out.println("신고당한지 15일 지나고 null로 바뀜? : " + checkDate);
		
		// 로그인시 유저의 신고 당한 이력이 있는지 신고 날짜 조회 
		// 신고 날짜가 있는 경우 
		String repDate = reService.selectMyReportedDate(m.getUser_id());
		
		System.out.println("신고 날짜 조회됨? : " + repDate);
		
		String writeActive;
		
		if(repDate != null) {
			writeActive = "n";
			System.out.println("writeActive : " + writeActive);
		}else {
			writeActive = "y";
			System.out.println("writeActive : " + writeActive);
		}
		
		model.addAttribute("writeActive", writeActive);
		
		
		
		
		Member loginUser = mService.loginMember(m);
		
		System.out.println("loginUser : " + loginUser);
		
		String referer = request.getHeader("Referer");
	    request.getSession().setAttribute("redirectURI", referer);
		System.out.println("이전페이지 :"+referer);
		referer = referer.substring(referer.lastIndexOf("goodbuy")+7);
		System.out.println("이전페이지 자른거:"+referer);
		// 일반 로그인이까 암호화 필요 o
		if (loginUser != null && bcryptPasswordEncoder.matches(m.getUser_pwd(), loginUser.getUser_pwd())) {
			// System.out.println("loginUser : " + loginUser);
			model.addAttribute("loginUser", loginUser);
			// 로그인 시 따로 호출하는 메소드
			saveUserTown(loginUser.getUser_id(), model);
			saveUserMtlist(loginUser.getUser_id(),model);
			
			// 뒤로 갈 히스토리가 있는 경우 및 우리 시스템에서 링크를 통해 유입된 경우
			return "redirect:"+referer;
		} else {
			model.addAttribute("msg", "로그인에 실패하였습니다.");
			return "redirect:/home";
		}
		
		
		
		
		
		
		
		
	}
	
	// 로그인시 유저의 첫번째,두번째 동네 이름만 가져와서 세션에 저장(메뉴바에 출력용)
	private void saveUserMtlist(String user_id, Model model) {
		List<String> mtlist = tService.selectMyTownList(user_id);
		if(mtlist != null) {
			model.addAttribute("mtlist", mtlist);
			System.out.println("멤버 컨트롤러(세션저장) mtlist : " + mtlist);
		}
	}
	
	

	// 회원가입 페이지로
	@GetMapping("/join")
	public ModelAndView selectAllTown(ModelAndView mv) {
		List<Town> tlist1 = tService.selectAllTown1();
		List<Town> tlist2 = tService.selectAllTown2();
		List<Town> tlist3 = tService.selectAllTown3();

		if (tlist1 != null && tlist2 != null && tlist3 != null) {
			mv.addObject("tlist1", tlist1);
			mv.addObject("tlist2", tlist2);
			mv.addObject("tlist3", tlist3);
			mv.setViewName("member/memberJoin");
		} else {
			mv.setViewName("member/memberJoin");
		}
//		    System.out.println(tlist1);
//		    System.out.println(tlist2);
//		    System.out.println(tlist3);
		return mv;
	}

	// 회원가입(DB insert)
	@PostMapping("/join")
	public String memberJoin(@ModelAttribute Member m, 
							Model model, 
							RedirectAttributes rd,
							@RequestParam String address_3
							) {

		String encPwd = bcryptPasswordEncoder.encode(m.getUser_pwd());
		m.setUser_pwd(encPwd);
		
		System.out.println("회원가입 넘어온 값 : "+m);
		System.out.println("넘어온 주소 값: "+address_3);
		
		// 1) 넘어온 주소값에 해당하는 t_no 조회하기
		int t_no = tService.selectTownNo(address_3);
		System.out.println(address_3 + "의 t_no : " + t_no);
		
		// 3) MEMBER insert
		int result = mService.insertMember(m);
		System.out.println("회원가입 잘됐나? : " + result);
		
		// 2) MYTOWN insert
		MyTown mt = new MyTown(m.getUser_id(), t_no);
		int insertMytown = tService.insertMyTown(mt);
		System.out.println("마이타운 객체확인 : " + mt);
		System.out.println("마이타운 들어갔나? : " + insertMytown);
		
		// 4) MYTOWN & MEMBER insert 성공 시
		if (result > 0) {
			// * RedirectAttributes : Redirect시 데이터를 전달할 수 있는 객체
			rd.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인 해주세요.");
			// redirect직전 모든 Flash 속성을 세션에 복사 -> redirect 직후 세션에서 다시 모델로 이동
			return "redirect:/home";

		} else {
			model.addAttribute("msg", "회원가입에 실패하였습니다.");
			return "common/errorpage";
		}

	}
	
	// 아이디 중복 체크 - "success or fail" - text 응답
	@PostMapping("/idCheck")
	public void userIdCheck(String user_id, HttpServletResponse response) {
		
		System.out.println("중복확인 할 아이디 넘어왔나" + user_id);
		
		int result = mService.userIdCheck(user_id);
		
		PrintWriter out;
		
		try {
			out = response.getWriter();
			
			if(result > 0) {
				out.print("fail");
			}else {
				out.print("success");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	// 회원 정보 수정
	@PostMapping("/update")
	public String updateMember( @ModelAttribute("loginUser") Member loginUser,
								Model model,
								RedirectAttributes rd,
								String nickname,
								String email,
								String phone,
								String address_3,
								String userNewPwd) {
		
		// 새롭게 바꿀 비번이 있을 시 암호화 후 셋팅
		if(!userNewPwd.equals("")) {
			String encPwd = bcryptPasswordEncoder.encode(userNewPwd);
			loginUser.setUser_pwd(encPwd);
		} else {
			// 비번 안바꿀 시 기존 비밀번호로 셋팅
			loginUser.setUser_pwd(loginUser.getUser_pwd());
		}
		
		loginUser.setNickname(nickname);
		loginUser.setEmail(email);
		loginUser.setPhone(phone);
		
		System.out.println("업데이트할 유저 정보" + loginUser);
		System.out.println("넘어온 address_3" + address_3);
		
		// USER_INFO update
		int result = mService.updateMember(loginUser);
		
		System.out.println("유저 정보 업데이트 됐나 : " + result);
		
		// MY_TOWN update 
		// 넘어온 주소값에 해당하는 t_no 조회하기
		int t_no = tService.selectTownNo(address_3);
		System.out.println("업데이트할 유저 동네 " + t_no);
		MyTown mt = new MyTown(loginUser.getUser_id(), t_no);
		
		// 현재 마이타운 타입만 변경,insert,delete 밖에 없으므로 update따로 만들어야함..ㅎ
		int result2 = tService.updateMyTown(mt);
		
		System.out.println("유저 동네 업네이드 됐나 : " + result2);
		
		// 바뀐 동네정보 다시 세션에 담아주기
		Town townInfo = tService.selectUserTown(loginUser.getUser_id());
		
		if(townInfo != null) {
			model.addAttribute("townInfo", townInfo);
			System.out.println("세션에 저장되는 타운 바뀌나 : " + townInfo);
		}
		
		return"redirect:/mypage/main";
		
	}
	
	// 회원정보 수정 시 / 탈퇴 시 기존 비밀번호 일치 해야 바꿔드림
	@PostMapping("originPwdCheck")
	public void originPwdCheck(String originPwd, HttpServletResponse response,@ModelAttribute("loginUser") Member m) {
		
		PrintWriter out;
			
				try {
					out = response.getWriter();
					if(bcryptPasswordEncoder.matches(originPwd,m.getUser_pwd())) {
					out.print("success");
					System.out.println("기존 비밀번호 일치!");
					}else {
						out.print("fail");
						System.out.println("기존 비밀번호 불일치!");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
	
	

	// 로그아웃 컨트롤러 (세션 만료)
	@GetMapping("/logout")
	public String logout(SessionStatus status, HttpServletRequest request) {

		status.setComplete();
		
		return "redirect:/home";
	}
	
	// 로그인 시 로그인 유저의 동네 정보(첫번째 기본동네) 세션에 저장하기
	public void saveUserTown(String user_id,
							   Model model
							 ) {
		
		Town townInfo = tService.selectUserTown(user_id);
		 
		if(townInfo != null) {
			model.addAttribute("townInfo", townInfo);
			System.out.println("멤버 컨트롤러(세션저장) townInfo" + townInfo);
		} 
		
	}
	
	
	// 탈퇴하기
	@GetMapping("/deleteMember")
	public String deleteMember(String user_id,SessionStatus status) {
		
		int result2 = mService.deleteMember(user_id);
		
		System.out.println("탈퇴유저 삭제됨? " + result2);
		
		if(result2 > 0) {
			// 로그아웃 시키기(세션만료)
			status.setComplete();
			return "redirect:/home";
		}else {
			return "common/errorpage"; 
		}
	}

	// 결제
	@GetMapping("payment")
	public String payment(int amount,int gno,
		@RequestParam(value="user_point", required=false, defaultValue="0") int user_point,
		HttpServletRequest request,
		Model model) {
		Member loginUser = (Member)request.getSession().getAttribute("loginUser");
		System.out.println("결제오니 ;"+amount +"원 포인트 : "+user_point+"gno : "+gno);
		int result = 0;
		//유저 포인트 빼기
		if(user_point>0) {
			result = mService.updatePoint(loginUser.getUser_id(), user_point,gno);
		}
		//안전거래 디비insert
		int result1 = mService.insertDeal(loginUser.getUser_id(), amount, gno);
		
		if(result>0||result1>0) {
			model.addAttribute("msg", "결제 성공");
		}else {
			
			model.addAttribute("msg", "결제 실패");
		}
		model.addAttribute("gno", gno);
		return "redirect:/goods/detail";
	}

	
	// 1. Stream을 이용한 text 응답
	@RequestMapping(value = "follow", method = RequestMethod.POST)
	public void follow(String seller, HttpServletResponse response, HttpServletRequest request) {
		System.out.println("여기오니?" + seller);
		try {
			PrintWriter out = response.getWriter();
			Member loginUser = (Member) request.getSession().getAttribute("loginUser");
			System.out.println("dd" + seller + "ddd" + loginUser.getUser_id());
			int result = mService.insertFollow(loginUser.getUser_id(), seller);

			if (result > 0) {
				out.write("success");
			} else {
				out.write("fail");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 1. Stream을 이용한 text 응답
	@RequestMapping(value = "unfollow", method = RequestMethod.POST)
	public void unfollow(String seller, HttpServletResponse response, HttpServletRequest request) {
		System.out.println("여기오니?" + seller);
		try {
			PrintWriter out = response.getWriter();
			Member loginUser = (Member) request.getSession().getAttribute("loginUser");
			System.out.println("dd" + seller + "ddd" + loginUser.getUser_id());
			int result = mService.canselFollow(loginUser.getUser_id(), seller);
			
			if (result > 0) {
				out.write("success");
			} else {
				out.write("fail");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 1. Stream을 이용한 text 응답 상품 찜하기 취소
	@PostMapping(value = "msgCount",produces = "application/json; charset= utf-8")
	public @ResponseBody String selectMsgCount(HttpServletResponse response, HttpServletRequest request) {
		System.out.println("쪽지관련 오니?");
		Member loginUser = (Member) request.getSession().getAttribute("loginUser");
		List<Messenger> mlist = null;
		Gson gson = null;
		if(loginUser!=null) {
			System.out.println("loginUser ; "+loginUser.getUser_id());
			
			mlist = mService.selectMsgList(loginUser.getUser_id());
			// 날짜 포맷하기 위해 GsonBuilder 를 이용해서 Gson객체 생성
			gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			System.out.println("새 쪽지dddd : "+mlist);
			
		}
		
		System.out.println("새 쪽지 : "+mlist);
		// 응답 작성
		return gson.toJson(mlist);
	}

	// 아이디 비밀번호 찾기
	@GetMapping("/find")
	public ModelAndView goFindIdPwdView(ModelAndView mv) {
		mv.setViewName("member/findUserInfo");
		return mv;
	}
		  
	// 아이디 찾기
	@PostMapping("/findId")
	public ModelAndView findId(String email,ModelAndView mv, Model model,RedirectAttributes rd) {
		
		String user_id = mService.findeUserId(email);
		
		System.out.println("아이디 찾아오니? " + user_id);
		
		if(user_id != null) {
			String nextId = user_id.substring(3,user_id.length());
			String star = "";
			for(int i = 0; i < nextId.length(); i++) {
				star += "*";
			}
			user_id = user_id.substring(0,3) + star;
			System.out.println(user_id);
			mv.addObject("user_id", user_id);
			mv.setViewName("member/findUserInfo");
		} else {
			mv.setViewName("member/findUserInfo");
		}
		
		return mv;
		
	}
	
	// 비밀번호 찾기
	@PostMapping("/findPwd")
	public  @ResponseBody void findPwd(String user_id, String email,
						Model model,HttpServletResponse response) {
		 response.setContentType("application/json; charset=utf-8");
		System.out.println("비번 찾 넘어온 user_id : " + user_id);
		System.out.println("비번 찾 넘어온 email : " + email);
		
	    double ran1 = Math.random();
	    double ran2 = Math.random();
	    double ran3 = Math.random();

	    char ranPwd1 = (char)((ran1 * 26) + 65);  // 대문자 랜덤
	    char ranPwd2 = (char)((ran2 * 26) + 65);  // 대문자 랜덤
	    int ranPwd3 = (int)((ran3 * 1000000) + 1);  // 숫자 랜덤
	    
	    String finalPwd = ranPwd1 + "" + ranPwd2 + "" + ranPwd3;

	    System.out.println("임시비번 : " + finalPwd);


		String encPwd = bcryptPasswordEncoder.encode(finalPwd);
		
		// 전달받은 아이디, 이메일 일치하는 유저의 비밀번호 -> 입시비번으로 업데이트 
		Member m = new Member();
		m.setUser_id(user_id);
		m.setEmail(email);
		m.setUser_pwd(encPwd);
		
		int result = mService.updateRandomPwd(m);
		
		if(result > 0) {
			JSONObject user = new JSONObject();
			
			user.put("user_id", m.getUser_id());
			user.put("email", m.getEmail());
			user.put("user_pwd", finalPwd);
			
			try {
				PrintWriter out = response.getWriter();
				out.print(user);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	@RequestMapping("/auth/kakao/callback")
	public String kakaoCallback(@RequestParam("code")String code, HttpSession session, Model model) {
		
		
		//1번인증 코드 요청 전달
		String accessToken = kakaoApi.getAccessToken(code);
		
		//2번  인증코드로 토큰 전달
		HashMap<String, Object> userInfo = kakaoApi.getUserInfo(accessToken);
		
		System.out.println("loginUser : "+userInfo);
		if(userInfo.get("email")!=null) {
			session.setAttribute("userId", userInfo.get("email"));
			session.setAttribute("access_token", accessToken);
			
		}
		int check= mService.userIdCheck((String) userInfo.get("id"));
		if(check==0){
			//회원가입 필요
			UUID garbagePass = UUID.randomUUID();
			
			Member kakaoMember = new Member();
			kakaoMember.setUser_id((String) userInfo.get("id"));
			kakaoMember.setNickname((String) userInfo.get("nickname"));
			kakaoMember.setEmail((String) userInfo.get("email"));
			kakaoMember.setUser_pwd(garbagePass.toString());
		

			int result = kakaoJoin(kakaoMember);
		
		}
		
		model.addAttribute("kakaoMember", (String) userInfo.get("id"));
		return "redirect:/member/kakaologin";
	}

	//카카오톡 회원가입 -> 간편로그인시 회원가입 통일
	public int kakaoJoin(Member km) {


		// 3) MEMBER insert
		int result = mService.insertKakaoMember(km);
		System.out.println("회원가입 잘됐나?dddd : " + result);

		// 2) MYTOWN insert
		MyTown mt = new MyTown(km.getUser_id(), 0);
		int insertMytown = tService.insertMyTown(mt);

		return result;

	}
	
	// 카카오톡 로그인 매서드 ->간편 로그인 통일 
	@GetMapping("/kakaologin") 
	public String kakaoLogin(@RequestParam("kakaoMember")String user_id, Model model,HttpServletRequest request) {

		int checkDate = reService.updateReportedDate(user_id);

		String repDate = reService.selectMyReportedDate(user_id);
		
		String writeActive;
		
		if(repDate != null) {
			writeActive = "n";
			System.out.println("writeActive : " + writeActive);
		}else {
			writeActive = "y";
			System.out.println("writeActive : " + writeActive);
		}
		
		model.addAttribute("writeActive", writeActive);
		
	
		Member loginUser = mService.kakaoLogin(user_id);
		

		if (loginUser != null) {
			model.addAttribute("loginUser", loginUser);
		
			saveUserTown(loginUser.getUser_id(), model);
			saveUserMtlist(loginUser.getUser_id(),model);
						
		} else {
			model.addAttribute("msg", "로그인에 실패하였습니다.");
		}
		return "redirect:/home";
		
	}
	
	@RequestMapping(value="/logout")
	public ModelAndView logout(HttpSession session) {
		ModelAndView mv = new ModelAndView();
		
		kakaoApi.kakaoLogout((String)session.getAttribute("access_token"));
		session.removeAttribute("access_token");
		
		session.removeAttribute("userId");
		mv.setViewName("home");
		return mv;
  }

	//msg확인했다는 표시 
	@PostMapping(value = "checkMsg", produces = "application/json; charset= utf-8")
	public @ResponseBody String checkMsgCount(int mno, HttpServletResponse response, HttpServletRequest request) {
		System.out.println("mno오니?"+mno);
		Member loginUser = (Member) request.getSession().getAttribute("loginUser");
		List<Messenger> mlist= mService.selectMsgListMno(mno, loginUser.getUser_id());
		// 날짜 포맷하기 위해 GsonBuilder 를 이용해서 Gson객체 생성
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		System.out.println("새 쪽지확인하고 다시 셀렉: " + mlist);

		// 응답 작성
		return gson.toJson(mlist);

	}
	
	
	//네이버 로그인 성공시 callback호출 메소드
    @RequestMapping(value = "/auth/naver/callback", method = { RequestMethod.GET, RequestMethod.POST })
    public String callback(Model model, @RequestParam String code, @RequestParam String state,HttpServletResponse response, HttpSession session)
            throws IOException {
        System.out.println("여기는 callback");
     // response를 위한 정의
     	PrintWriter writer = response.getWriter();
        OAuth2AccessToken oauthToken;
        oauthToken = naverLoginBO.getAccessToken(session, code, state);
        //로그인 사용자 정보를 읽어온다.
        apiResult = naverLoginBO.getUserProfile(oauthToken);
        System.out.println(naverLoginBO.getUserProfile(oauthToken).toString());
        model.addAttribute("result", apiResult);
        System.out.println("result : "+apiResult);
		// 2. String형식인 apiResult를 json형태로 바꿈
		JSONParser parser = new JSONParser();
		Object obj;
		try {
			obj = parser.parse(apiResult);
			org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) obj;
			// 3. 데이터 파싱
			// Top레벨 단계 _response 파싱
			org.json.simple.JSONObject response_obj = (org.json.simple.JSONObject) jsonObj.get("response");
			// 네이버에서 주는 고유 ID
			String naverIfId = (String) response_obj.get("id");
			// 네이버에서 설정된 사용자 별명
			String naverNickname = (String) response_obj.get("nickname");
			// 네이버에서 설정된 이메일
			String naverEmail = (String) response_obj.get("email");
			
			System.out.println("naverIfId "+naverIfId);
			System.out.println("naverNickname "+naverNickname);
			System.out.println("naverEmail "+naverEmail);
			
			int check= mService.userIdCheck(naverIfId);
			if(check==0){
				//회원가입 필요
				UUID garbagePass = UUID.randomUUID();
				
				Member naverMember = new Member();
				naverMember.setUser_id(naverIfId);
				naverMember.setNickname(naverNickname);
				naverMember.setEmail(naverEmail);
				naverMember.setUser_pwd(garbagePass.toString());
				System.out.println("여기여기 "+naverMember);

				int result = kakaoJoin(naverMember);
				System.out.println("join성공 ?"+result);
			}
			model.addAttribute("kakaoMember", (String)naverIfId);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return "redirect:/member/kakaologin";
    }
 // 1. Stream을 이용한 text 응답 상품 찜하기 취소
 	@PostMapping(value = "/alram",produces = "application/json; charset= utf-8")
 	public @ResponseBody String selectAlarmList(HttpServletResponse response, HttpServletRequest request) {
 		System.out.println("알람관련오니?");
 		Member loginUser = (Member) request.getSession().getAttribute("loginUser");
 		List<Alarm> alist = null;
 		Gson gson = null;
 		if(loginUser!=null) {
 			System.out.println("loginUser ; "+loginUser.getUser_id());
 			
 			alist = mService.selectAlarmList(loginUser.getUser_id());
 			// 날짜 포맷하기 위해 GsonBuilder 를 이용해서 Gson객체 생성
 			gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
 			System.out.println("새 알림  : "+alist);
 			
 		}
 		
 		System.out.println("새알림 : "+alist);
 		System.out.println("새알림 길이 : "+alist.size());
 		// 응답 작성
 		return gson.toJson(alist);
 	}

	// alarm확인했다는 표시
	@PostMapping(value = "checkAlarm", produces = "application/json; charset= utf-8")
	public @ResponseBody String checkAlarmCount(int mno, HttpServletResponse response, HttpServletRequest request) {
		System.out.println("ajax mno오니?" + mno);
		
		Member loginUser = (Member) request.getSession().getAttribute("loginUser");
 		List<Alarm> alist = null;
 		Gson gson = null;
 		if(loginUser!=null) {
 			System.out.println("loginUser ; "+loginUser.getUser_id());
 			
 			alist = mService.selectAlarmListAno(mno, loginUser.getUser_id());
 			// 날짜 포맷하기 위해 GsonBuilder 를 이용해서 Gson객체 생성
 			gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
 			System.out.println("새 알림  : "+alist);
 			
 		}
 		
 		System.out.println("새알림 : "+alist);
		// 응답 작성
		return gson.toJson(alist);

	}

	// 판매자 메모 수정 
	@RequestMapping(value = "updateUC", method = RequestMethod.POST)
	public void likegoods(String comment, HttpServletResponse response, HttpServletRequest request) {
		System.out.println("코멘트"+comment);
		try {
			PrintWriter out = response.getWriter();
			Member loginUser = (Member) request.getSession().getAttribute("loginUser");
			int ok = mService.updateUserComment(loginUser.getUser_id(), comment);
			if (ok > 0) {
				out.write("success");
			} else {
				out.write("fail");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
