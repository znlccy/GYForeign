package com.gy.servicesImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.asm.Type;
import com.gy.dao.UserDao;
import com.gy.message.DomesticMessage;
import com.gy.message.ForeignMessage;
import com.gy.message.MessageCode;
import com.gy.model.Account;
import com.gy.model.Game;
import com.gy.model.Order;
import com.gy.model.User;
import com.gy.services.AccountService;
import com.gy.services.GameService;
import com.gy.services.UserService;
import com.gy.util.JwtUtil;
import com.gy.util.RandomCode;
import com.gy.util.SplitString;

/**
 * @author Chencongye
 * @version 0.0.1
 * @introduce 这是用户服务层接口实现类
 * @date 2017.9.12
 */

@Service
public class UserServiceImpl implements UserService {
	
	/**
	 * 自动装配
	 */
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	/**
	 * 在自动注入的时候，需要生成set和get方法
	 * @return
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	/**
	 * 根据Id查询用户
	 */
	@Override
	public User query(int userid) {
		// TODO Auto-generated method stub
		return userDao.query(userid);
	}

	/**
	 * 查询所有用户
	 */
	@Override
	public List<User> queryAll() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 保存用户
	 */
	@Override
	public boolean save(User user) {
		return userDao.save(user);
	}

	/**
	 * 保存所有用户
	 */
	@Override
	public boolean saveAll(User[] users) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 根据Id删除用户
	 */
	@Override
	public boolean delete(int userid) {
		// TODO Auto-generated method stub
		return userDao.delete(userid);
	}

	/**
	 * 删除所有用户
	 */
	@Override
	public boolean deleteAll(User[] User) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 更新用户
	 */
	@Override
	public boolean update(User user) {
		// TODO Auto-generated method stub
		return userDao.update(user);
	}

	/**
	 * 跟新所有用户
	 */
	@Override
	public boolean updateAll(User[] users) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 根据SQL语句来查询所有用户
	 */
	@Override
	public User querysql(String sql) {
		// TODO Auto-generated method stub
		return userDao.querysql(sql);
	}

	/**
	 * 根据SQL语句查询用户
	 */
	@Override
	public User queryBysql(String sql) {
		// TODO Auto-generated method stub
		return userDao.queryBysql(sql);
	}

	/**
	 * 保存或者更新用户
	 */
	@Override
	public boolean saveorupdate(User user) {
		// TODO Auto-generated method stub
		return userDao.saveorupdate(user);
	}
	
	/**
	 * 实现登录功能
	 */
	public void login(User user,Map map,String type) {
		// TODO Auto-generated method stub
		/*1.先判断用户以什么样的方式登录*/
		/*String type = user.getType().trim();*/
		/*String type = null;*/
		String status = null;
		String message = null;
		int userid = 0;
		int gameid = 0;
		
		/*2.一般登录的检索用户所用的账户类型*/
		String sql = "from User u where u.username=" +  "'" + user.getUsername() + "'"+"or u.mobile="+"'"+user.getMobile()+"'"+" or u.email="+"'"+user.getEmail()+"'"+" and u.password="+"'"+user.getPassword()+"'";
		User userdata = null;
		
		switch (type) {
			/*1以用户名加密码的方式登录*/
			case "1":
				
				if(!(user.getUsername().equals("") || "".equals(user.getUsername())) && !(user.getPassword().equals("") || "".equals(user.getPassword())))
				{
					try {
						userdata = this.querysql(sql);
						/*4.先判断用户输入的数据是否为空*/
						
						if(userdata!=null)
						{
							boolean flag = (userdata.getUsername().equals(user.getUsername())) && (userdata.getPassword().equals(user.getPassword()));
							if(flag){
								status = "0200";
								message = "普通用户名模式登录成功！";
								userid = userdata.getUserid();
								
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								if(games.size()<=0)
								{
									status = "0200";
									message = "普通用户名模式登录成功,但是传入的游戏为空！";
									userid = userdata.getUserid();
								}
								else
								{
									
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(userdata);
									
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+userdata.getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							else
							{
								status = "0403";
								message = "登录失败,用户名或者密码可能输入错误！";
								userid = userdata.getUserid();
							}
						}
						else
						{
							status = "0404";
							message = "不存在该用户！";
							userid = 0;
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					status = "0403";
					message = "用户名或者密码输入值为空！";
					userid = 0;
				}	
				break;
			/*2以邮箱加密码的方式登录*/
			case "2":
				if(!(user.getEmail().equals("") || "".equals(user.getEmail())) && !(user.getPassword().equals("") || "".equals(user.getPassword())))
				{
					try {
						userdata = this.querysql(sql);
						
						/*4.先判断用户输入的数据是否为空*/
						
						if(userdata!=null)
						{
							boolean flag = (userdata.getEmail().equals(user.getEmail())) && (userdata.getPassword().equals(user.getPassword()));
							if(flag){
								status = "0200";
								message = "普通邮箱模式登录成功！";
								userid = userdata.getUserid();
								
								
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								if(games.size()<=0)
								{
									status = "0200";
									message = "普通邮箱模式登录成功,但是传入的游戏为空！";
									userid = userdata.getUserid();
								}
								else
								{
									System.err.println("登录保存游戏数据！");
									
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(userdata);
									
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+userdata.getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									/*Date logintime = user.getLogintime();
									String subject = JwtUtil.generalSubject(user, gamedata);
									String token = JwtUtil.createJWT(id, subject, ttlMillis);*/
								}
							}
							else
							{
								status = "0403";
								message = "登录失败,邮箱或者密码可能输入错误！";
								userid = userdata.getUserid();
							}
						}
						else
						{
							status = "0404";
							message = "不存在该用户！";
							userid = 0;
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					status = "0403";
					message = "邮箱或者密码输入值为空！";
					userid = 0;
				}	
				break;
			case "3":
				if(!(user.getMobile().equals("") || "".equals(user.getMobile())) && !(user.getPassword().equals("") || "".equals(user.getPassword())))
				{
					try {
						userdata = this.querysql(sql);
						
						/*4.先判断用户输入的数据是否为空*/
						
						if(userdata!=null)
						{
							boolean flag = (userdata.getMobile().equals(user.getMobile())) && (userdata.getPassword().equals(user.getPassword()));
							if(flag){
								status = "0200";
								message = "普通手机模式登录成功！";
								userid = userdata.getUserid();
								
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								if(games.size()<=0)
								{
									status = "0200";
									message = "普通手机模式登录成功，但是传入的游戏为空！";
									userid = userdata.getUserid();
								}
								else
								{
									
									
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(userdata);
									
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+userdata.getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
							else
							{
								status = "0403";
								message = "登录失败,手机或者密码可能输入错误！";
								userid = userdata.getUserid();
							}
						}
						else
						{
							status = "0404";
							message = "不存在该用户！";
							userid = 0;
						}
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					status = "0403";
					message = "手机或者密码输入值为空！";
					userid = 0;
				}	
				break;
			case "4":
				/*这是facebook方式登录*/
				System.err.println("这是facebook方式登录");
				/*message = "这是facebook登录！";*/
				/*1.如果是第一次登录那么进行，写入数据库*/
				Account account = null;
				Set<Account> accounts = new HashSet<Account>();

				accounts = user.getAccounts();
				if(accounts.size()>0)
				{
					Iterator<Account> acc = accounts.iterator(); 
					while(acc.hasNext())
					{
						account = acc.next();
						/*System.err.println(account.getAccountname());*/
						/*System.err.println(account);*/
					}
					if(account.getAccountname().equals("") || "".equals(account.getAccountname()))
					{
						status = "0403";
						message = "用户输入的账号为空!";
						userid = 0;
					}
					else
					{
						String accsql = "from Account where accountname="+"'"+account.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
						Account accountdata = accountService.querysql(accsql);
						System.err.println("打印账户数据:"+accountdata);
						if(accountdata==null)
						{
							status = "0200";
							message = "第一次登录数据库不存在重新生成账户！！";
							String username = account.getAccountname();
							/*userid = 0;*/
							account.setAccounttype(user.getType());
							user.setRegisttime(new Date());
							user.setAccounts(user.getAccounts());
							user.setEmail("");
							user.setMobile("");
							user.setUsername(account.getAccountname());
							user.setPassword("");
							user.setType(type);
							this.save(user);
							
							User udata = this.querysql("from User where username="+"'"+account.getAccountname().trim()+"'"+"and type="+"'"+user.getType()+"'");
							User usernew  = new User();
							usernew.setUserid(udata.getUserid());
							account.setUser(usernew);
							accountService.saveorupdate(account);
							
							String idsql = "from Account where accountname="+"'"+account.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
							Account accountdatanew = accountService.querysql(idsql);
							System.err.println(accountdatanew);
							if(accountdatanew==null)
							{
								status = "0404";
								message = "类型参数错误，空指针";
								userid = 0;
							}
							else
							{
								userid = accountdatanew.getUser().getUserid();
							}
							Set<Game> games = new HashSet<Game>();
							games = user.getGames();
							
							if(games.size()<=0)
							{
								status = "0200";
								message = "登录成功,但是传入的游戏为空！";
								userid = udata.getUserid();
							}
							else
							{
								System.err.println("登录保存游戏数据！");
								Iterator<Game> game = games.iterator();
								
								Game gamedata = new Game();
								
								while(game.hasNext())
								{
									gamedata = game.next();
								}
								gamedata.setUser(user);
								
								gameService.saveorupdate(gamedata);
								
								/*查询游戏*/
								String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+udata.getUserid()+"'";
								Game gamenew = gameService.queryBysql(gamesql);
								gameid = gamenew.getGameid();
								map.put("gameid", gameid);
								
								String subject = JwtUtil.generalSubject(user, gamedata);
								long ttlMillis = System.currentTimeMillis();
								try {
									String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
									map.put("token", token);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}
						else
						{
							if(accountdata.getAccounttype().equals(user.getType()) && accountdata.getAccountname().equals(account.getAccountname()))
							{
								status = "0200";
								message = "第三方Facebook登录成功！";
								userid = accountdata.getUser().getUserid();
								/*1.如果用这个账户去登录别的游戏*/
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								
								if(games.size()<=0)
								{
									status = "0200";
									message = "登录成功,但是传入的游戏为空！";
									userid = accountdata.getUser().getUserid();
								}
								else
								{
									System.err.println("登录时保存游戏数据！");
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(accountdata.getUser());
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+accountdata.getUser().getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
				else
				{
					status = "0404";
					message = "用户没有填写第三方登录信息！！！";
					userid = 0;
				}
				
				break;
			case "5":
				/*这是twitter方式登录*/
				System.err.println("这是twitter方式登录");
				message = "这是twitter方式登录！";
				/*1.如果是第一次登录那么进行，写入数据库*/
				Account account1 = null;
				Set<Account> accounts1 = new HashSet<Account>();
				accounts1 = user.getAccounts();
				if(accounts1.size()>0)
				{
					Iterator<Account> acc = accounts1.iterator(); 
					while(acc.hasNext())
					{
						account1 = acc.next();
						System.err.println(account1.getAccountname());
						System.err.println(account1);
					}
					if(account1.getAccountname().equals("") || "".equals(account1.getAccountname()))
					{
						status = "0403";
						message = "用户输入的账号为空!";
						userid = 0;
					}
					else
					{
						String accsql = "from Account where accountname="+"'"+account1.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
						Account accountdata1 = accountService.querysql(accsql);
						System.err.println(accountdata1);
						if(accountdata1==null)
						{
							status = "0200";
							message = "第一次登录数据库不存在重新生成账户！！";
							String username = account1.getAccountname();
							account1.setAccounttype(user.getType());
							user.setRegisttime(new Date());
							user.setAccounts(user.getAccounts());
							user.setEmail("");
							user.setMobile("");
							user.setUsername(account1.getAccountname());
							user.setPassword("");
							user.setType(type);
							System.err.println("保存数据");
							this.save(user);
							
							User udata = this.querysql("from User where username="+"'"+account1.getAccountname().trim()+"'"+"and type="+"'"+user.getType()+"'");
							User usernew  = new User();
							usernew.setUserid(udata.getUserid());
							System.err.println("打印用户数据:"+usernew);
							account1.setUser(usernew);
							accountService.saveorupdate(account1);
							
							String idsql = "from Account where accountname="+"'"+account1.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
							Account accountdatanew1 = accountService.querysql(idsql);
							if(accountdatanew1==null)
							{
								status = "0404";
								message = "类型参数错误，空指针";
								userid = 0;
							}
							else
							{
								userid = accountdatanew1.getUser().getUserid();
							}
							Set<Game> games = new HashSet<Game>();
							games = user.getGames();
							
							if(games.size()<=0)
							{
								status = "0200";
								message = "登录成功,但是传入的游戏为空！";
								userid = accountdatanew1.getUser().getUserid();
							}
							else
							{
								System.err.println("登录时保存游戏数据！");
								Iterator<Game> game = games.iterator();
								
								Game gamedata = new Game();
								
								while(game.hasNext())
								{
									gamedata = game.next();
								}
								gamedata.setUser(user);
								
								gameService.saveorupdate(gamedata);
								
								/*查询游戏*/
								String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+accountdatanew1.getUser().getUserid()+"'";
								Game gamenew = gameService.queryBysql(gamesql);
								gameid = gamenew.getGameid();
								map.put("gameid", gameid);
								
								String subject = JwtUtil.generalSubject(user, gamedata);
								long ttlMillis = System.currentTimeMillis();
								try {
									String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
									map.put("token", token);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}
						else
						{
							if(accountdata1.getAccounttype().equals(user.getType()) && accountdata1.getAccountname().trim().equals(account1.getAccountname()))
							{
								status = "0200";
								message = "第三方Twitter登录成功！";
								userid = accountdata1.getUser().getUserid();
								
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								
								if(games.size()<=0)
								{
									status = "0200";
									message = "登录成功,但是传入的游戏为空！";
									userid = accountdata1.getUser().getUserid();
								}
								else
								{
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(accountdata1.getUser());
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+accountdata1.getUser().getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
				else
				{
					status = "0404";
					message = "用户没有填写第三方登录信息！！！";
					userid = 0;
				}
				break;
			case "6":
				/*这是googleplay方式登录*/
				System.err.println("这是googleplay方式登录");
				message = "这是googleplay方式登录！";
				/*1.如果是第一次登录那么进行，写入数据库*/
				Account account2 = null;
				Set<Account> accounts2 = new HashSet<Account>();
				accounts2 = user.getAccounts();
				if(accounts2.size()>0)
				{
					Iterator<Account> acc = accounts2.iterator(); 
					while(acc.hasNext())
					{
						account2 = acc.next();
						System.err.println(account2.getAccountname());
						System.err.println(account2);
					}
					if(account2.getAccountname().equals("") || "".equals(account2.getAccountname()))
					{
						status = "0403";
						message = "用户输入的账号为空!";
						userid = 0;
					}
					else
					{
						String accsql = "from Account where accountname="+"'"+account2.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
						Account accountdata2 = accountService.querysql(accsql);
						System.err.println(accountdata2);
						if(accountdata2==null)
						{
							status = "0200";
							message = "第一次登录数据库不存在重新生成账户！！";
							account2.setAccounttype(user.getType());
							String username = account2.getAccountname();
							user.setRegisttime(new Date());
							user.setAccounts(user.getAccounts());
							user.setEmail("");
							user.setMobile("");
							user.setUsername(account2.getAccountname());
							user.setPassword("");
							user.setType(type);
							/*System.err.println("保存数据");*/
							this.save(user);
							
							User udata = this.querysql("from User where username="+"'"+account2.getAccountname().trim()+"'"+"and type="+"'"+user.getType()+"'");
							User usernew  = new User();
							usernew.setUserid(udata.getUserid());
							account2.setUser(usernew);
							accountService.saveorupdate(account2);
							
							String idsql = "from Account where accountname="+"'"+account2.getAccountname().trim()+"'"+"and accounttype="+"'"+user.getType()+"'";
							Account accountdatanew2 = accountService.querysql(idsql);
							if(accountdatanew2==null)
							{
								status = "0404";
								message = "类型参数错误，空指针";
								userid = 0;
							}
							else
							{
								userid = accountdatanew2.getUser().getUserid();
							}
							Set<Game> games = new HashSet<Game>();
							games = user.getGames();
							if(games.size()<=0)
							{
								status = "0200";
								message = "登录成功，但是传入的游戏为空！";
								userid = accountdatanew2.getUser().getUserid();
							}
							else
							{
								System.err.println("登录时保存游戏数据！");
								Iterator<Game> game = games.iterator();
								
								Game gamedata = new Game();
								
								while(game.hasNext())
								{
									gamedata = game.next();
								}
								
								gamedata.setUser(user);
								
								gameService.saveorupdate(gamedata);
								
								/*查询游戏*/
								String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+accountdatanew2.getUser().getUserid()+"'";
								Game gamenew = gameService.queryBysql(gamesql);
								gameid = gamenew.getGameid();
								map.put("gameid", gameid);
								
								String subject = JwtUtil.generalSubject(user, gamedata);
								long ttlMillis = System.currentTimeMillis();
								try {
									String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
									map.put("token", token);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						else
						{
							if(accountdata2.getAccounttype().equals(user.getType()) && accountdata2.getAccountname().equals(account2.getAccountname()))
							{
								status = "0200";
								message = "第三方GooglePlay登录成功！";
								userid = accountdata2.getUser().getUserid();
								Set<Game> games = new HashSet<Game>();
								games = user.getGames();
								
								if(games.size()<=0)
								{
									status = "0200";
									message = "登录成功，但是传入的游戏为空！";
									userid = accountdata2.getUser().getUserid();
								}
								else
								{
									System.err.println("登录时保存游戏数据！");
									Iterator<Game> game = games.iterator();
									
									Game gamedata = new Game();
									
									while(game.hasNext())
									{
										gamedata = game.next();
									}
									gamedata.setUser(accountdata2.getUser());
									gameService.saveorupdate(gamedata);
									
									/*查询游戏*/
									String gamesql = "from Game where gamepackage="+"'"+gamedata.getGamepackage()+"'"+"and userid="+"'"+accountdata2.getUser().getUserid()+"'";
									Game gamenew = gameService.queryBysql(gamesql);
									gameid = gamenew.getGameid();
									map.put("gameid", gameid);
									
									String subject = JwtUtil.generalSubject(user, gamedata);
									long ttlMillis = System.currentTimeMillis();
									try {
										String token = JwtUtil.createJWT(String.valueOf(RandomCode.getRandNum(1, 9999)), subject, ttlMillis);
										map.put("token", token);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
				else
				{
					status = "0404";
					message = "用户没有填写第三方登录信息！！！";
					userid = 0;
				}
				break;
				
		}
		
		map.put("status", status);
		map.put("message", message);
		map.put("userid", userid);
	}

	/**
	 * 实现注册功能
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void register(Map map) {
		// TODO Auto-generated method stub
		String status = null;
		String message = null;
		int userid = 0;
		String password = ((String)map.get("password")).trim();
		String type = ((String)map.get("type")).trim();
		String valicode = ((String)map.get("valicode")).trim();
		/*//获得国内验证码
		String valicodeServer = DomesticMessage.getVerificationCode();*/
		//获得国际验证码
		String valicodeServer = ForeignMessage.getVerificationCode();
		
		
		/*//获得国内验证码
		String valicodeServer = DomesticMessage.getVerificationCode();*/
		//获得国际验证码
		/*String valicodeServer = ForeignMessage.getVerificationCode();*/
		
		if(valicode.equals("") || "".equals(valicode))
		{
			status = "0403";
			message = "验证码输入为空值！";
			userid = 0;
		}
		else
		{
			if(valicode.equals(valicodeServer))
			{
				/*2.先判断数据库中是否存在这个用户*/
				/*String sql = "from User u where u.username=" + "'" + username + "'"+"or u.mobile="+"'"+mobile+"'"+"or u.email="+"'"+email+"'";
				User userdata = userService.querysql(sql);*/
				
				switch (type) {
				case "1":
					String username = ((String)map.get("username")).trim();
					if(!(username.equals("") || "".equals(username)) && !(password.equals("") || "".equals(password)))
					{
						String namesql = "from User u where u.username=" + "'" + username+"'"+"and u.password="+"'"+password+"'";
						User userdata = this.querysql(namesql);
						if(userdata!=null)
						{
							status = "0202";
							message = "该用户已经注册了，请重新填写！";
							userid = userdata.getUserid();
							map.remove("useid");
						}
						else
						{
							/*4.如果数据库中不存在那么进行注册*/
							User user = new User();
							/*user.setEmail(email);*/
							user.setRegisttime(new Date());
							user.setType(type);
							user.setEmail("");
							user.setMobile("");
							user.setUsername(username);
							user.setPassword(password);
							
							Game game = SplitString.getGame(map);
							if(game==null)
							{
								status = "0403";
								message = "游戏数据为空";
								userid = 0;
								if(this.save(user)){
									status = "0200";
									message = "用户名注册成功,但是没有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
								}
							}
							else
							{
								Set<Game> gameset = new HashSet<Game>();
								gameset.add(game);
								user.setGames(gameset);
								if(this.save(user)){
									status = "0200";
									message = "用户名注册成功,带有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
									/*2.生成Token给客户端*/
									/*subject = JwtUtil.generalSubject(user1);
									token = jwt.createJWT(Constant.JWT_ID, subject, Constant.JWT_TTL);
									2.注册成功需要把相应的token值存储到token表中
									map.put("token", token);*/
								}
							}
						}
					}
					else 
					{
						status = "0403";
						message = "用户输入的用户名或者密码为空！";
						userid = 0;
					}
					break;
				case "2":
					String email = ((String)map.get("email")).trim();
					if(!(email.equals("") || "".equals(email)) && !(password.equals("") || "".equals(password)))
					{
						String namesql = "from User u where u.email=" + "'" +email+"'"+"and u.password="+"'"+password+"'";
						User userdata = this.querysql(namesql);
						if(userdata!=null)
						{
							status = "0202";
							message = "该用户已经注册了，请重新填写！";
							userid = userdata.getUserid();
						}
						else
						{
							/*4.如果数据库中不存在那么进行注册*/
							User user = new User();
							/*user.setEmail(email);*/
							user.setRegisttime(new Date());
							user.setType(type);
							user.setEmail(email);
							user.setUsername("");
							user.setMobile("");
							user.setPassword(password);
							Game game = SplitString.getGame(map);
							if(game==null)
							{
								status = "0403";
								message = "游戏数据为空";
								userid = 0;
								if(this.save(user)){
									status = "0200";
									message = "邮箱注册成功,但是没有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
								}
							}
							else
							{
								Set<Game> gameset = new HashSet<Game>();
								gameset.add(game);
								user.setGames(gameset);
								if(this.save(user)){
									status = "0200";
									message = "邮箱注册成功，带有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
								}
							}
						}
					}
					else 
					{
						status = "0403";
						message = "用户输入的邮箱或者密码为空！";
						userid = 0;
					}
					break;
				case "3":
					String mobile = ((String)map.get("mobile")).trim();
					if(!(mobile.equals("") || "".equals(mobile)) && !(password.equals("") || "".equals(password)))
					{
						String namesql = "from User u where u.mobile=" + "'"+mobile+"'"+"and u.password="+"'"+password+"'";
						User userdata = this.querysql(namesql);
						if(userdata!=null)
						{
							status = "0202";
							message = "该用户已经注册了，请重新填写！";
							userid = userdata.getUserid();
						}
						else
						{
							/*4.如果数据库中不存在那么进行注册*/
							User user = new User();
							/*user.setEmail(email);*/
							user.setRegisttime(new Date());
							user.setType(type);
							user.setMobile(mobile);
							user.setEmail("");
							user.setUsername("");
							user.setPassword(password);
							
							Game game = SplitString.getGame(map);
							
							if(game==null)
							{
								status = "0403";
								message = "游戏数据为空";
								userid = 0;
								if(this.save(user)){
									status = "0200";
									message = "手机注册成功,但是没有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
								}
							}
							else
							{
								Set<Game> gameset = new HashSet<Game>();
								gameset.add(game);
								user.setGames(gameset);
								
								if(this.save(user)){
									status = "0200";
									message = "手机注册成功，带有游戏数据!";
									User user1 = (User)this.querysql("from User where username="+"'"+user.getUsername()+"'");
									userid = user1.getUserid();
								}
							}
						}
					}
					else 
					{
						status = "0403";
						message = "用户输入的电话或者密码为空！";
						userid = 0;
					}
					break;
				}
			}
			else
			{
				status = "0402";
				message = "验证码填写错误！";
				userid = 0;
			}
		}
		
		/*这一段用来确定用户输入的参数是否有误*/
		/*if (bindingResult.hasErrors()) {
			map.put("errorCode", "40001");
			map.put("errorMsg", bindingResult.getFieldError().getDefaultMessage());
		}*/
		
		map.remove("username");
		map.remove("password");
		map.remove("valicode");
		map.remove("mobile");
		map.remove("email");
		map.remove("type");
		map.remove("games");
		/*map.put("username", username);*/
		map.put("status", status);
		map.put("message", message);
		map.put("userid", userid);
	}

	/**
	 * 实现忘记密码一功能
	 */
	@Override
	public void forgetpassone(Map map) {
		// TODO Auto-generated method stub
		String status = null;
		String message = null;
		int userid = 0;
		String mobile = ((String)map.get("mobile")).trim();
		String valicode = ((String)map.get("valicode")).trim();
		
		/*//获得国内验证码
		String valicodeServer = DomesticMessage.getVerificationCode();*/
		//获得国际验证码
		String valicodeServer = ForeignMessage.getVerificationCode();
		
		
		/*1.先根据用户提供的手机号，查询数据库中是否存在这个用户如果存在则返回为真*/
		/*1.1 先判断用户输入的内容不能为空 */
		String sql = "from User u where u.mobile="+"'"+mobile+"'";
		User userdata = null;
		if(valicode.equals("") || "".equals(valicode))
		{
			status = "0404";
			message = "用户输入的验证码有空！";
			userid = 0;
		}
		else
		{
			if(valicode.equals(valicodeServer))
			{
				boolean judge = (mobile.equals("") || "".equals(mobile));
				if(judge)
				{
					status = "0404";
					message = "用户输入的手机号为空！";
					userid = 0;
				}
				else
				{
					userdata = this.querysql(sql);
					if(userdata!=null)
					{
						status = "0200";
						message = "成功，进行下一步！";
						userid = userdata.getUserid();
					}
					else
					{
						status = "0404";
						message = "登录不成功，数据库中不存在这个账户！";
						userid = 0;
					}
				}
			}
			else
			{
				status = "0404";
				message = "验证码不对";
				userid = 0;
			}
			
		}
		
        map.remove("mobile");
        map.remove("valicode");
        map.put("status", status);
        map.put("message", message);
        map.put("userid", userid);
	}

	/**
	 * 实现忘记密码功能
	 */
	@Override
	public void forgetpasstwo(Map map) {
		// TODO Auto-generated method stub
		
		String status = null;
		String message = null;
		int userid = 0;
		String mobile = ((String)map.get("mobile")).trim();
		String password = ((String)map.get("password")).trim();
		String confirmpass = ((String)map.get("confirmpass")).trim();
		
		/*1.先在数据库中查找这个用户*/
		String sql = "from User u where u.mobile="+"'"+mobile.trim()+"'";
		User userdata = null;
		boolean judge = (mobile.equals("") || "".equals(mobile));
		if(judge)
		{
			status = "0404";
			message = "手机号为空！";
			userid = 0;
		}
		else
		{
			/*2.更新这个用户的密码*/
			userdata = this.queryBysql(sql);
			if(userdata != null)
			{
				if((password.equals("")||"".equals(password)) || (confirmpass.equals("")||"".equals(confirmpass)))
				{
					status = "0404";
					message = "用户输入的密码或者确认密码有空值！";
					userid = userdata.getUserid();
				}
				else
				{
					if(confirmpass.equals(password))
					{
						String passworddata = userdata.getPassword();
						
						if(confirmpass.equals(passworddata))
						{
							status = "0403";
							message = "不能使用之前的密码！！！";
							userid = userdata.getUserid();
						}
						else
						{
							userdata.setPassword(confirmpass);
							
							this.update(userdata);
							
							status = "0200";
							message = "更新密码成功！！！";
							userid = userdata.getUserid();
						}
					}
					else
					{
						status = "0403";
						message = "两次输入的密码不一致！";
						userid = userdata.getUserid();
					}
				}
			}
			else
			{
				status = "0404";
				message = "不存在这个账户，请重新输入！";
				userid = 0;
			}
		}
		
		map.remove("mobile");
		map.remove("password");
		map.remove("confirmpass");
		map.put("status", status);
		map.put("message", message);
		map.put("userid", userid);
	}

}
