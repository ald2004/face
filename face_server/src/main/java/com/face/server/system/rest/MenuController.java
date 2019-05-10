package com.face.server.system.rest;

import com.face.server.system.domain.Menu;
import com.face.server.system.domain.User;
import com.face.server.common.aop.log.Log;
import com.face.server.common.exception.BadRequestException;
import com.face.server.core.utils.JwtTokenUtil;
import com.face.server.system.service.MenuService;
import com.face.server.system.service.UserService;
import com.face.server.system.service.dto.MenuDTO;
import com.face.server.system.service.query.MenuQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author jie
 * @date 2018-12-03
 */
@RestController
@RequestMapping("api")
public class MenuController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuQueryService menuQueryService;

    @Autowired
    private UserService userService;

    private static final String ENTITY_NAME = "menu";

    @GetMapping(value = "/menus/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_SELECT')")
    public ResponseEntity<MenuDTO> getMenus(@PathVariable Long id){
        return new ResponseEntity<>(menuService.findById(id), HttpStatus.OK);
    }

    /**
     * 构建前端路由所需要的菜单
     * @return
     */
    @GetMapping(value = "/menus/build")
    public ResponseEntity<Object> buildMenus(HttpServletRequest request){
        User user = userService.findByName(jwtTokenUtil.getUserName(request));
        List<MenuDTO> menuDTOList = menuService.findByRoles(user.getRoles());
        return new ResponseEntity<>(menuService.buildMenus((List<MenuDTO>)menuService.buildTree(menuDTOList).get("content")),HttpStatus.OK);
    }

    /**
     * 返回全部的菜单
     * @return
     */
    @GetMapping(value = "/menus/tree")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_SELECT')")
    public ResponseEntity<Object> getMenuTree(){
        return new ResponseEntity<>(menuService.getMenuTree(menuService.findByPid(0L)),HttpStatus.OK);
    }

    @Log(description = "查询菜单")
    @GetMapping(value = "/menus")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_SELECT')")
    public ResponseEntity<java.util.Map> getMenus(@RequestParam(required = false) String name){
        List<MenuDTO> menuDTOList = menuQueryService.queryAll(name);
        return new ResponseEntity<>(menuService.buildTree(menuDTOList),HttpStatus.OK);
    }

    @Log(description = "新增菜单")
    @PostMapping(value = "/menus")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_CREATE')")
    public ResponseEntity<MenuDTO> create(@Validated @RequestBody Menu resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        return new ResponseEntity<>(menuService.create(resources),HttpStatus.CREATED);
    }

    @Log(description = "修改菜单")
    @PutMapping(value = "/menus")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_EDIT')")
    public ResponseEntity update(@Validated @RequestBody Menu resources){
        if (resources.getId() == null) {
            throw new BadRequestException(ENTITY_NAME +" ID Can not be empty");
        }
        menuService.update(resources);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Log(description = "删除菜单")
    @DeleteMapping(value = "/menus/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENU_ALL','MENU_DELETE')")
    public ResponseEntity delete(@PathVariable Long id){
        menuService.delete(id);
        return new ResponseEntity(HttpStatus.OK);
    }
}
