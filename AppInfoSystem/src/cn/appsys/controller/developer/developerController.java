package cn.appsys.controller.developer;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import cn.appsys.pojo.DevUser;
import cn.appsys.service.developer.AppCategoryService;
import cn.appsys.service.developer.AppInfoService;
import cn.appsys.service.developer.DataDictionaryService;
import cn.appsys.service.developer.DevUserService;
import cn.appsys.tools.Constants;
@Controller
@RequestMapping("devuser")
public class developerController {
	@Resource
	DevUserService devUserService;
	@RequestMapping("/login")
	public String login(){
		
		return "devlogin";
	}
	@RequestMapping("/devlogin")
	public String login2(@RequestParam String devCode,@RequestParam String devPassword,Model model,HttpSession session){
		DevUser devUser=devUserService.login(devCode, devPassword);
		session.setAttribute(Constants.DEV_USER_SESSION, devUser);
		model.addAttribute("devUser", devUser);
		return "developer/main";
	}
}
