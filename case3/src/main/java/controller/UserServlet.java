package controller;

import dao.ICustomerDAO;
import dao.IProductDAO;
import dao.IRoleDAO;
import dao.IUserDAO;
import dao.impl.CustomerDAO;
import dao.impl.ProductDAO;
import dao.impl.RoleDAO;
import dao.impl.UserDAO;
import model.User;
import utils.AppUtils;
import utils.ValidateUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = "/user")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final IUserDAO userDAO = UserDAO.getInstance();
    private final IRoleDAO roleDAO = RoleDAO.getInstance();
    private final IProductDAO productDAO = ProductDAO.getInstance();
    private final ICustomerDAO customerDAO = CustomerDAO.getInstance();

    @Override
    public void init() {
        if (this.getServletContext().getAttribute("listUser") == null) {
            this.getServletContext().setAttribute("listUser", userDAO.selectAllUser());
        } else {
            updateListUser();
        }

        if (this.getServletContext().getAttribute("listRole") == null) {
            this.getServletContext().setAttribute("listRole", roleDAO.selectAllRole());
        }

        if (this.getServletContext().getAttribute("listProduct") == null) {
            this.getServletContext().setAttribute("listProduct", productDAO.selectAllProduct());
        }

        if (this.getServletContext().getAttribute("listCustomer") == null) {
            this.getServletContext().setAttribute("listCustomer", customerDAO.selectAllCustomer());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
//        if (session.getAttribute("account") == null) {
//            response.sendRedirect("/login?type=user");
//            return;
//        }
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                showCreateForm(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            case "delete":
                deleteUser(request, response);
                break;
            case "view":
                showViewForm(request, response);
                break;
            case "reset":
                resetPass(request, response);
                break;
            case "changepass":
                showChangePassForm(request, response);
                break;
            default:
                listUser(request, response);
                break;
        }
    }

    private void showChangePassForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User account = (User) session.getAttribute("account");
        RequestDispatcher requestDispatcher;
        if (userDAO.selectUser(account.getId()) == null) {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/404.jsp");
            requestDispatcher.forward(request, response);
        } else {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/changepass.jsp");
            requestDispatcher.forward(request, response);
        }
    }

    private void showViewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        RequestDispatcher requestDispatcher;
        User user = userDAO.selectUser(id);
        if (user == null) {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/404.jsp");
            requestDispatcher.forward(request, response);
        } else {
            request.setAttribute("user", user);
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/view.jsp");
            requestDispatcher.forward(request, response);
        }
    }

    private void resetPass(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        RequestDispatcher requestDispatcher;
        User user = userDAO.selectUser(id);
        if (user == null) {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/404.jsp");
        } else {
            user.setPassword("123456");
            user.setUpdatedTime(LocalDateTime.now());
            userDAO.updateUser(user);
            request.setAttribute("message", "?????t l???i m???t kh???u th??nh c??ng");
            request.setAttribute("user", user);
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/edit.jsp");
        }
        requestDispatcher.forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        RequestDispatcher requestDispatcher;
        User user = userDAO.selectUser(id);
        if (user == null) {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/404.jsp");
            requestDispatcher.forward(request, response);
        } else {
            request.setAttribute("user", user);
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/edit.jsp");
            requestDispatcher.forward(request, response);
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher;
        String id = request.getParameter("id");
        User user = userDAO.selectUser(id);
        if (user == null) {
            requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/404.jsp");
            requestDispatcher.forward(request, response);
        }
        userDAO.deleteUser(id);
        response.sendRedirect("/user");
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/create.jsp");
        dispatcher.forward(request, response);
    }

    private void listUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = 1;
        int recordsPerPage = 5;
        String q = "";
        int roleId = -1;
        if (request.getParameter("recordsPerPage") != null) {
            recordsPerPage = Integer.parseInt(request.getParameter("recordsPerPage"));
        }
        if (request.getParameter("q") != null) {
            q = request.getParameter("q");
        }
        if (request.getParameter("roleId") != null) {
            roleId = Integer.parseInt(request.getParameter("roleId"));
        }
        if (request.getParameter("page") != null)
            page = Integer.parseInt(request.getParameter("page"));

        List<User> userList = userDAO.selectAllUsersPaggingFilter((page - 1) * recordsPerPage, recordsPerPage, q, roleId);
        int noOfRecords = userDAO.getNoOfRecords();
        int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / recordsPerPage);

        request.setAttribute("listUser", userList);
        request.setAttribute("noOfPages", noOfPages);
        request.setAttribute("currentPage", page);
        request.setAttribute("recordsPerPage", recordsPerPage);

        request.setAttribute("q", q);
        request.setAttribute("roleId", roleId);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/list.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        switch (action) {
            case "create":
                createUser(request, response);
                break;
            case "edit":
                editUser(request, response);
                break;
            case "search":
                searchUser(request, response);
                break;
            case "changepass":
                changePassword(request, response);
                break;
        }
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> errors = new ArrayList<>();
        RequestDispatcher requestDispatcher;
        String password = request.getParameter("password");
        String passwordReEnter = request.getParameter("passwordReEnter");

        if (password.trim().equals("")) {
            errors.add("M???t kh???u kh??ng ???????c ????? tr???ng");
        }
        if (!ValidateUtils.isPasswordValid(password)) {
            errors.add("M???t kh???u c?? ????? d??i 8-20 k?? t???, ch???a ??t nh???t 1 ch??? c??i vi???t hoa, vi???t th?????ng, ch??? s???, k?? t ?????c bi???t");
        }

        if (passwordReEnter.trim().equals("")) {
            errors.add("M???t kh???u kh??ng ???????c ????? tr???ng");
        }
        if (!ValidateUtils.isPasswordValid(passwordReEnter)) {
            errors.add("M???t kh???u c?? ????? d??i 8-20 k?? t???, ch???a ??t nh???t 1 ch??? c??i vi???t hoa, vi???t th?????ng, ch??? s???, k?? t ?????c bi???t");
        }

        if (!password.equals(passwordReEnter)) {
            errors.add("M???t kh???u kh??ng tr??ng nhau");
        }

        if (errors.isEmpty()) {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("account");
            user.setPassword(password);
            userDAO.updateUser(user);
            request.setAttribute("message", "?????i m???t kh???u th??nh c??ng");
        } else {
            request.setAttribute("errors", errors);
            request.setAttribute("password",password);
            request.setAttribute("passwordReEnter",passwordReEnter);
        }
        requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/changepass.jsp");
        requestDispatcher.forward(request, response);

    }

    private void editUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher requestDispatcher;
        List<String> errors = new ArrayList<>();
        String id, fullName, phoneNumber, email, address, image;
        LocalDate birthDay;
        int role;

        id = request.getParameter("id");
        User oldUser = userDAO.selectUser(id);
        User newUser = new User();
        if (oldUser == null) {
            errors.add("Nh??n vi??n kh??ng t???n t???i");
        } else {
            newUser.setId(id);
            fullName = request.getParameter("fullName");
            if (fullName.trim().equals("")) errors.add("H??? t??n kh??ng ???????c ????? tr???ng");
            newUser.setFullName(fullName);

            try {
                birthDay = AppUtils.stringToLocalDate(request.getParameter("birthDay"));
                newUser.setBirthDay(birthDay);
            } catch (Exception e) {
                errors.add("?????nh d???ng ng??y sinh kh??ng h???p l???");
            }

            phoneNumber = request.getParameter("phoneNumber");
            if (!ValidateUtils.isPhoneValid(phoneNumber))
                errors.add("S??? ??i???n tho???i kh??ng h???p l??? (S??? ??i???n tho???i bao g???m 10 ch??? s???)");
            if (!oldUser.getPhoneNumber().equals(phoneNumber)) {
                if (userDAO.selectUserByPhoneNumber(phoneNumber) != null)
                    errors.add("S??? ??i???n tho???i ???? t???n t???i trong h??? th???ng)");
            }
            newUser.setPhoneNumber(phoneNumber);

            email = request.getParameter("email");
            if (!ValidateUtils.isEmailValid(email))
                errors.add("Email kh??ng h???p l??? (S??? ??i???n tho???i bao g???m 10 ch??? s???)");
            if (!oldUser.getEmail().equals(email)) {
                if (userDAO.selectUserByEmail(email) != null)
                    errors.add("Email ???? t???n t???i trong h??? th???ng)");
            }
            newUser.setEmail(email);

            address = request.getParameter("address");
            if (address.trim().equals("")) errors.add("?????a ch??? kh??ng ???????c ????? tr???ng");
            newUser.setAddress(address);

            try {
                role = Integer.parseInt(request.getParameter("role"));
                if (roleDAO.selectRole(role) == null) {
                    errors.add("Quy???n h???n kh??ng t???n t???i trong h??? th???ng)");
                } else {
                    newUser.setRole(role);
                }
            } catch (NumberFormatException e) {
                errors.add("?????nh d???ng quy???n kh??ng h???p l???");
            }

            image = request.getParameter("image");
            if (!ValidateUtils.isImageValid(image))
                errors.add("???????ng d???n ???nh kh??ng ????ng (???????ng d???n ???nh ph???i c?? ??u??i l?? jpg/png/jpeg)");
            newUser.setImage(image);
        }

        if (errors.isEmpty()) {
            newUser.setPassword(oldUser.getPassword());
            newUser.setUpdatedTime(LocalDateTime.now());
            userDAO.updateUser(newUser);
            request.setAttribute("message", "C???p nh???t nh??n vi??n" + " ' " + oldUser.getFullName() + " ' " + "th??nh c??ng");
            updateListUser();
            request.setAttribute("user", newUser);
        } else {
            request.setAttribute("errors", errors);
            request.setAttribute("user", oldUser);
        }
        requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/edit.jsp");
        requestDispatcher.forward(request, response);
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User newUser = new User();
        RequestDispatcher requestDispatcher;
        List<String> errors = new ArrayList<>();
        String id = "US" + System.currentTimeMillis() / 1000;
        LocalDate birthDay = null;
        int role = -1;

        String fullName = request.getParameter("fullName");
        if (fullName.trim().equals("")) errors.add("H??? t??n kh??ng ???????c ????? tr???ng");
        newUser.setFullName(fullName);
        try {
            birthDay = AppUtils.stringToLocalDate(request.getParameter("birthDay"));
            newUser.setBirthDay(birthDay);
        } catch (Exception e) {
            errors.add("?????nh d???ng ng??y sinh kh??ng h???p l???");
        }

        String phoneNumber = request.getParameter("phoneNumber");
        if (!ValidateUtils.isPhoneValid(phoneNumber))
            errors.add("S??? ??i???n tho???i kh??ng h???p l??? (S??? ??i???n tho???i bao g???m 10 ch??? s???)");
        if (userDAO.selectUserByPhoneNumber(phoneNumber) != null)
            errors.add("S??? ??i???n tho???i ???? t???n t???i trong h??? th???ng)");
        newUser.setPhoneNumber(phoneNumber);

        String email = request.getParameter("email");
        if (!ValidateUtils.isEmailValid(email))
            errors.add("Email kh??ng h???p l??? (S??? ??i???n tho???i bao g???m 10 ch??? s???)");
        if (userDAO.selectUserByEmail(email) != null)
            errors.add("Email ???? t???n t???i trong h??? th???ng)");
        newUser.setEmail(email);

        String address = request.getParameter("address");
        if (address.trim().equals("")) errors.add("?????a ch??? kh??ng ???????c ????? tr???ng");
        newUser.setAddress(address);

        try {
            role = Integer.parseInt(request.getParameter("role"));
            if (roleDAO.selectRole(role) == null)
                errors.add("Quy???n h???n kh??ng t???n t???i trong h??? th???ng)");
            newUser.setRole(role);
        } catch (NumberFormatException e) {
            errors.add("?????nh d???ng quy???n kh??ng h???p l???");
        }

        String image = request.getParameter("image");
        if (!ValidateUtils.isImageValid(image))
            errors.add("???????ng d???n ???nh kh??ng ????ng (???????ng d???n ???nh ph???i c?? ??u??i l?? jpg/png/jpeg)");
        newUser.setImage(image);

        if (errors.isEmpty()) {
            User user = new User(id, fullName, birthDay, phoneNumber, email, address, image, role);
            user.setPassword("123456");
            user.setCreatedTime(LocalDateTime.now());
            userDAO.insertUser(user);
            request.setAttribute("message", "Th??m m???i nh??n vi??n" + " ' " + fullName + " ' " + "th??nh c??ng (t??i kho???n:" + " ' " + email + " ' " + "v?? m???t kh???u m???c ?????nh: '123456')");
            updateListUser();
        } else {
            request.setAttribute("user", newUser);
            request.setAttribute("errors", errors);
        }
        requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/create.jsp");
        requestDispatcher.forward(request, response);
    }

    private void searchUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchStr = request.getParameter("inputSearch");
        List<User> userListSearch = userDAO.searchUser(searchStr);
        request.setAttribute("userListSearch", userListSearch);
        request.setAttribute("searchStr", searchStr);
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/WEB-INF/dashboard/user/search.jsp");
        requestDispatcher.forward(request, response);
    }

    private void updateListUser() {
        this.getServletContext().removeAttribute("listUser");
        this.getServletContext().setAttribute("listUser", userDAO.selectAllUser());
    }
}
