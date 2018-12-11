package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 判断session是否有效  session有效就去主页 不然就返回登陆界面
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getConnection();
		try {
			//加 false 使得没有session也不创建新session
			HttpSession session = request.getSession(false);
			JSONObject obj = new JSONObject();
			if(session != null) {
				String userId = session.getAttribute("user_id").toString();
				obj.put("result", "SUCCESS").put("user_id", userId).put("name", conn.getFullname(userId));
			}else {
				response.setStatus(403);
				obj.put("result", "User Doesn't Exist");
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * // 如果用户名密码存在  建立一个新session
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			//读request
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			JSONObject obj = new JSONObject();
			if(connection.verifyLogin(userId, password)) {
				// Returns the current session associated with this request,or if the request does not have a session, creates one.
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);
				//返回前端 便于debug
				obj.put("result", "SUCCESS").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				response.setStatus(401);
				obj.put("result", "User Doesn't Exist");
			}
			RpcHelper.writeJsonObject(response, obj);
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			connection.close();
		}
	}

}
