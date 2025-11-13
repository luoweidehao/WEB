/**
 * 导航栏认证状态管理脚本
 * 在所有页面的 </body> 前添加: <script src="auth-navbar.js"></script>
 */

(function() {
    'use strict';

    // 检查用户登录状态
    function checkAuthStatus() {
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        
        if (token && userStr) {
            try {
                const user = JSON.parse(userStr);
                return { isLoggedIn: true, user: user };
            } catch (e) {
                console.error('解析用户信息失败:', e);
                return { isLoggedIn: false, user: null };
            }
        }
        return { isLoggedIn: false, user: null };
    }

    // 退出登录
    function logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    }

    // 更新导航栏
    function updateNavbar() {
        const { isLoggedIn, user } = checkAuthStatus();
        
        // 更新顶部通知栏的登录/注册链接
        const topBarLoginLink = document.querySelector('.bg-primary a[href*="login"]');
        if (topBarLoginLink) {
            if (isLoggedIn) {
                topBarLoginLink.innerHTML = `<i class="fa fa-user mr-1"></i> ${user.username}`;
                topBarLoginLink.href = '#';
                topBarLoginLink.onclick = (e) => {
                    e.preventDefault();
                    showUserMenu(e);
                };
            }
        }

        // 更新桌面端导航栏
        const desktopNav = document.querySelector('header nav.hidden.md\\:flex');
        if (desktopNav && isLoggedIn) {
            // 移除"加入会员"按钮,添加用户菜单
            const joinButton = desktopNav.querySelector('button');
            if (joinButton && joinButton.textContent.includes('加入会员')) {
                // 创建用户下拉菜单
                const userMenu = document.createElement('div');
                userMenu.className = 'relative';
                userMenu.innerHTML = `
                    <button id="user-menu-btn" class="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition">
                        <i class="fa fa-user-circle"></i>
                        <span>${user.username}</span>
                        <i class="fa fa-chevron-down text-xs"></i>
                    </button>
                    <div id="user-dropdown" class="hidden absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
                        <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                            <i class="fa fa-user mr-2"></i>个人资料
                        </a>
                        <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                            <i class="fa fa-cog mr-2"></i>账户设置
                        </a>
                        <hr class="my-1">
                        <a href="#" id="logout-btn" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                            <i class="fa fa-sign-out mr-2"></i>退出登录
                        </a>
                    </div>
                `;
                
                joinButton.replaceWith(userMenu);
                
                // 添加下拉菜单交互
                const menuBtn = document.getElementById('user-menu-btn');
                const dropdown = document.getElementById('user-dropdown');
                const logoutBtn = document.getElementById('logout-btn');
                
                if (menuBtn && dropdown) {
                    menuBtn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        dropdown.classList.toggle('hidden');
                    });
                    
                    // 点击外部关闭菜单
                    document.addEventListener('click', () => {
                        dropdown.classList.add('hidden');
                    });
                }
                
                if (logoutBtn) {
                    logoutBtn.addEventListener('click', (e) => {
                        e.preventDefault();
                        if (confirm('确定要退出登录吗?')) {
                            logout();
                        }
                    });
                }
            }
        }

        // 更新移动端导航栏
        const mobileMenu = document.getElementById('mobileMenu');
        if (mobileMenu && isLoggedIn) {
            const mobileJoinButton = mobileMenu.querySelector('button');
            if (mobileJoinButton && mobileJoinButton.textContent.includes('加入会员')) {
                const userInfo = document.createElement('div');
                userInfo.className = 'py-3 border-t border-gray-100';
                userInfo.innerHTML = `
                    <div class="flex items-center gap-3 mb-3">
                        <i class="fa fa-user-circle text-primary text-2xl"></i>
                        <span class="font-medium text-gray-700">${user.username}</span>
                    </div>
                    <a href="#" class="block py-2 text-gray-700 hover:text-primary">
                        <i class="fa fa-user mr-2"></i>个人资料
                    </a>
                    <a href="#" class="block py-2 text-gray-700 hover:text-primary">
                        <i class="fa fa-cog mr-2"></i>账户设置
                    </a>
                    <button id="mobile-logout-btn" class="w-full mt-2 bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-600 transition">
                        <i class="fa fa-sign-out mr-2"></i>退出登录
                    </button>
                `;
                
                mobileJoinButton.replaceWith(userInfo);
                
                const mobileLogoutBtn = document.getElementById('mobile-logout-btn');
                if (mobileLogoutBtn) {
                    mobileLogoutBtn.addEventListener('click', (e) => {
                        e.preventDefault();
                        if (confirm('确定要退出登录吗?')) {
                            logout();
                        }
                    });
                }
            }
        }
    }

    // 显示简单的用户菜单 (用于顶部栏)
    function showUserMenu(event) {
        const existingMenu = document.getElementById('top-user-menu');
        if (existingMenu) {
            existingMenu.remove();
            return;
        }

        const menu = document.createElement('div');
        menu.id = 'top-user-menu';
        menu.className = 'absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50';
        menu.style.top = event.target.getBoundingClientRect().bottom + 'px';
        menu.style.right = '1rem';
        menu.innerHTML = `
            <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                <i class="fa fa-user mr-2"></i>个人资料
            </a>
            <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                <i class="fa fa-cog mr-2"></i>账户设置
            </a>
            <hr class="my-1">
            <a href="#" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100" onclick="event.preventDefault(); if(confirm('确定要退出登录吗?')) { localStorage.clear(); window.location.href='index.html'; }">
                <i class="fa fa-sign-out mr-2"></i>退出登录
            </a>
        `;

        document.body.appendChild(menu);

        setTimeout(() => {
            document.addEventListener('click', function closeMenu() {
                menu.remove();
                document.removeEventListener('click', closeMenu);
            });
        }, 100);
    }

    // 页面加载完成后更新导航栏
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', updateNavbar);
    } else {
        updateNavbar();
    }
})();