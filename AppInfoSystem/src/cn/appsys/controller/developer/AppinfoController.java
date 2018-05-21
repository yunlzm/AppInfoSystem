package cn.appsys.controller.developer;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;

import cn.appsys.pojo.AppCategory;
import cn.appsys.pojo.AppInfo;
import cn.appsys.pojo.AppVersion;
import cn.appsys.pojo.DataDictionary;
import cn.appsys.pojo.DevUser;
import cn.appsys.service.developer.AppCategoryService;
import cn.appsys.service.developer.AppInfoService;
import cn.appsys.service.developer.AppVersionService;
import cn.appsys.service.developer.DataDictionaryService;
import cn.appsys.tools.Constants;
import cn.appsys.tools.PageSupport;
@Controller
@RequestMapping("/app")
public class AppinfoController {
	@Resource
	AppInfoService AppInfoService;
	@Resource 
	AppCategoryService AppCategoryService;
	@Resource
	DataDictionaryService DataDictionaryService;
	@Resource
	AppVersionService AppVersionService;
	@RequestMapping("/appinfolist")
	public String applist(@RequestParam(value="querySoftwareName",required=false) String querySoftwareName,@RequestParam(value="_queryStatus",required=false) String _queryStatus,
			@RequestParam(value="queryCategoryLevel1",required=false) String _queryCategoryLevel1,@RequestParam(value="_queryCategoryLevel2",required=false) String _queryCategoryLevel2,@RequestParam(value="_queryCategoryLevel3",required=false) String _queryCategoryLevel3,@RequestParam(value="_queryFlatformId",required=false) String _queryFlatformId,
			@RequestParam(value="pageIndex",required=false) Integer pageSizeIndex,
			Model model,HttpSession session){
		Integer devId = ((DevUser)session.getAttribute(Constants.DEV_USER_SESSION)).getId();
		List<AppInfo> appInfoList = null;
		List<DataDictionary> statusList = null;
		List<DataDictionary> flatFormList = null;
		List<AppCategory> categoryLevel1List = null;//列出一级分类列表，注：二级和三级分类列表通过异步ajax获取
		List<AppCategory> categoryLevel2List = null;
		List<AppCategory> categoryLevel3List = null;
		//页面容量
		int pageSize = Constants.pageSize;
		//当前页码
		Integer currentPageNo = 1;
		
		if(pageSizeIndex != null){
			try{
				currentPageNo = Integer.valueOf(pageSizeIndex);
			}catch (NumberFormatException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		Integer queryStatus = null;
		if(_queryStatus != null && !_queryStatus.equals("")){
			queryStatus = Integer.parseInt(_queryStatus);
		}
		Integer queryCategoryLevel1  = null;
		if(_queryCategoryLevel1 != null && !_queryCategoryLevel1.equals("")){
			queryCategoryLevel1 = Integer.parseInt(_queryCategoryLevel1);
		}
		Integer queryCategoryLevel2 = null;
		if(_queryCategoryLevel2 != null && !_queryCategoryLevel2.equals("")){
			queryCategoryLevel2 = Integer.parseInt(_queryCategoryLevel2);
		}
		Integer queryCategoryLevel3 = null;
		if(_queryCategoryLevel3 != null && !_queryCategoryLevel3.equals("")){
			queryCategoryLevel3 = Integer.parseInt(_queryCategoryLevel3);
		}
		Integer queryFlatformId = null;
		if(_queryFlatformId != null && !_queryFlatformId.equals("")){
			queryFlatformId = Integer.parseInt(_queryFlatformId);
		}
		
		//总数量（表）
		int totalCount = 0;
		try {
			totalCount = AppInfoService.getAppInfoCount(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		//控制首页和尾页
		if(currentPageNo < 1){
			currentPageNo = 1;
		}else if(currentPageNo > totalPageCount){
			currentPageNo = totalPageCount;
		}
		try {
			appInfoList = AppInfoService.getAppInfoList(querySoftwareName, queryStatus, queryCategoryLevel1, queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId, currentPageNo, pageSize);
			statusList = this.getDataDictionaryList("APP_STATUS");
			flatFormList = this.getDataDictionaryList("APP_FLATFORM");
			categoryLevel1List = AppCategoryService.getAppCategoryListByParentId(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("appInfoList", appInfoList);
		model.addAttribute("statusList", statusList);
		model.addAttribute("flatFormList", flatFormList);
		model.addAttribute("categoryLevel1List", categoryLevel1List);
		model.addAttribute("pages", pages);
		model.addAttribute("queryStatus", queryStatus);
		model.addAttribute("querySoftwareName", querySoftwareName);
		model.addAttribute("queryCategoryLevel1", queryCategoryLevel1);
		model.addAttribute("queryCategoryLevel2", queryCategoryLevel2);
		model.addAttribute("queryCategoryLevel3", queryCategoryLevel3);
		model.addAttribute("queryFlatformId", queryFlatformId);
		
		//二级分类列表和三级分类列表---回显
		if(queryCategoryLevel2 != null && !queryCategoryLevel2.equals("")){
			categoryLevel2List = getCategoryList(queryCategoryLevel1.toString());
			model.addAttribute("categoryLevel2List", categoryLevel2List);
		}
		if(queryCategoryLevel3 != null && !queryCategoryLevel3.equals("")){
			categoryLevel3List = getCategoryList(queryCategoryLevel2.toString());
			model.addAttribute("categoryLevel3List", categoryLevel3List);
		}
		return "developer/appinfolist";
	}
	
	
	
	/**
	 * 根据typeCode查询出相应的数据字典列表
	 * @param pid
	 * @return
	 */
	@RequestMapping(value="/datadictionarylist.json",method=RequestMethod.GET,produces="application/json;charset=utf-8")
	@ResponseBody
	public List<DataDictionary> getDataDicList (@RequestParam String tcode){
		//logger.debug("getDataDicList tcode ============ " + tcode);
		return this.getDataDictionaryList(tcode);
	}
	
	@RequestMapping(value="/datadictionarylist2.json",method=RequestMethod.GET,produces="application/json;charset=utf-8")
	@ResponseBody
	public Object getDataDicList2 (@RequestParam String tcode){
		//logger.debug("getDataDicList tcode ============ " + tcode);
		return JSONArray.toJSONString(this.getDataDictionaryList(tcode));
	}
	/**
	 * 根据parentId查询出相应的分类级别列表
	 * @param pid
	 * @return
	 */
	public List<DataDictionary> getDataDictionaryList(String typeCode){
		List<DataDictionary> dataDictionaryList = null;
		try {
			dataDictionaryList = DataDictionaryService.getDataDictionaryList(typeCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataDictionaryList;
	}
	
	@RequestMapping(value="/categorylevellist.json",method=RequestMethod.GET,produces="application/json;charset=utf-8")
	@ResponseBody
	public Object getAppCategoryList (@RequestParam String pid){
		if(pid.equals("")) pid = null;
		return JSONArray.toJSONString(getCategoryList(pid));
	}
	
	public List<AppCategory> getCategoryList (String pid){
		List<AppCategory> categoryLevelList = null;
		try {
			categoryLevelList = AppCategoryService.getAppCategoryListByParentId(pid==null?null:Integer.parseInt(pid));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return categoryLevelList;
	}
	
	@RequestMapping(value="/deleteVersionByAppId",method=RequestMethod.GET)
	@ResponseBody
	public Object deleteVersionByAppId(@RequestParam  String id) throws Exception{
		Integer appid=Integer.parseInt(id);
		HashMap<String, String> map=new HashMap<String,String>();
		int count=AppVersionService.getVersionCountByAppId(appid);
		boolean delResult;
		if(count>0) {
			delResult=AppVersionService.deleteVersionByAppId(appid);
			delResult=AppInfoService.deleteAppInfoById(appid);
			if(delResult) {
				map.put("delResult", "true");
				
			}else {
				map.put("delResult", "false");
			}
		}else {
			delResult=AppInfoService.deleteAppInfoById(appid);
			if(delResult) {
				map.put("delResult", "true");
				
			}else {
				map.put("delResult", "false");
			}
		}
		
		return JSONArray.toJSONString(map);
	}

@RequestMapping(value="/appinfoadd",method=RequestMethod.GET)

public String appinfoadd() throws Exception {
	
	return "developer/appinfoadd";
}
	//添加信息
	@RequestMapping("/addappinfoadd")
	public String add(HttpServletRequest request,AppInfo appInfo,HttpSession session) throws Exception {
		
		appInfo.setDevId(((DevUser)session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		boolean flag=AppInfoService.add(appInfo);
		
		
		if(flag) {
			return "redirect:/app/appinfolist";
		}else {
			return "redirect:/app/appinfoadd";
		}
	}
	//根据id查询信息
	@RequestMapping(value="/appinfomodify",method=RequestMethod.GET)
	public String appinfomodify(HttpServletRequest request,Model model) throws Exception {
		int id=Integer.parseInt(request.getParameter("id"));
		AppInfo appInfo=AppInfoService.appinfolist(id);
		model.addAttribute("appInfo", appInfo);
		return "developer/appinfomodify";
	}
	@RequestMapping(value="/appinfomodifysave")
	public String appinfomodifysave(HttpServletRequest request,Model model,AppInfo appInfo) throws Exception {
		boolean flag=AppInfoService.modify(appInfo);
		if(flag) {
			return "redirect:/app/appinfolist";
		}else {
			return "redirect:/app/appinfoadd";
		}
	}
	//跳转到版本添加页面
	@RequestMapping(value="/appversionadd",method=RequestMethod.GET)
	public String appversionadd(@RequestParam(value="id")String appId, AppVersion appVersion,Model model) throws Exception {
		appVersion.setAppId(Integer.parseInt(appId));
		model.addAttribute(appVersion);
		return "developer/appversionadd";
	}
	//添加版本信息
		@RequestMapping(value="/addversionsave")
		public String addversionsave(AppVersion appVersion,Model model,HttpSession session,HttpServletRequest request) throws Exception {
			boolean flag=AppVersionService.appsysadd(appVersion);
			if(flag) {
				return "redirect:/app/appinfolist";
			}else {
				return "redirect:/app/appversionadd";
			}
		}
		//跳转到版本修改页面
		@RequestMapping(value="/appversionmodify",method=RequestMethod.GET)
		public String appversionmodify(@RequestParam(value="vid")String appId, AppVersion appVersion,Model model) throws Exception {
			 appVersion=AppVersionService.getAppVersionById(Integer.parseInt(appId));
			 model.addAttribute("appVersion", appVersion);
			return "developer/appversionmodify";
		}
		//修改版本信息
		@RequestMapping(value="/appversionmodifysave")
		public String appversionmodifysave(AppVersion appVersion,Model model,HttpSession session,HttpServletRequest request) throws Exception {
			boolean flag=AppVersionService.modify(appVersion);
			if(flag) {
				return "redirect:/app/appinfolist";
			}else {
				return "redirect:/app/appversionmodifysave";
			}
		}
		//跳转到查看页面
		@RequestMapping(value="/appinfoview",method=RequestMethod.GET)
		public String appinfoview(@RequestParam(value="id")String id,Model model) throws Exception {
			AppInfo appInfo=AppInfoService.getAppInfo(Integer.parseInt(id),null);
			List<AppVersion> appVersionList = AppVersionService.getAppVersionList(Integer.parseInt(id));
			model.addAttribute("appVersionList", appVersionList);
			model.addAttribute("appInfo", appInfo);
			return "developer/appinfoview";
		}
		@RequestMapping(value="/{appid}/sale",method=RequestMethod.PUT)
		@ResponseBody
		public Object sale(@PathVariable String appid,HttpSession session){
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			Integer appIdInteger = 0;
			try{
				appIdInteger = Integer.parseInt(appid);
			}catch(Exception e){
				appIdInteger = 0;
			}
			resultMap.put("errorCode", "0");
			resultMap.put("appId", appid);
			if(appIdInteger>0){
				try {
					DevUser devUser = (DevUser)session.getAttribute(Constants.DEV_USER_SESSION);
					AppInfo appInfo = new AppInfo();
					appInfo.setId(appIdInteger);
					appInfo.setModifyBy(devUser.getId());
					if(AppInfoService.appsysUpdateSaleStatusByAppId(appInfo)){
						resultMap.put("resultMsg", "success");
					}else{
						resultMap.put("resultMsg", "success");
					}		
				} catch (Exception e) {
					resultMap.put("errorCode", "exception000001");
				}
			}else{
				//errorCode:0为正常
				resultMap.put("errorCode", "param000001");
			}
			
			/*
			 * resultMsg:success/failed
			 * errorCode:exception000001
			 * appId:appId
			 * errorCode:param000001
			 */
			return resultMap;
		}
}
