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

    // 从服务器获取最新的用户信息
    async function refreshUserInfo() {
        const token = localStorage.getItem('token');
        if (!token) {
            return null;
        }

        try {
            const response = await fetch('http://localhost:8080/api/me', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const userInfo = await response.json();
                // 更新localStorage中的用户信息
                localStorage.setItem('user', JSON.stringify(userInfo));
                return userInfo;
            } else if (response.status === 401) {
                // Token无效，清除登录信息
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                localStorage.removeItem('adminToken');
                localStorage.removeItem('isAdmin');
                return null;
            }
        } catch (error) {
            console.error('获取用户信息失败:', error);
            // 网络错误时使用缓存的用户信息
        }
        return null;
    }

    // 退出登录
    function logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    }

    // 更新导航栏
    async function updateNavbar() {
        let { isLoggedIn, user } = checkAuthStatus();
        
        // 如果用户已登录，从服务器获取最新的用户信息（包括membership状态）
        if (isLoggedIn) {
            const freshUserInfo = await refreshUserInfo();
            if (freshUserInfo) {
                user = freshUserInfo;
            } else if (!user) {
                // 如果获取失败且没有缓存，说明未登录
                isLoggedIn = false;
            }
        }
        
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
                
                // 先移除所有可能存在的管理员链接（包括登录入口和管理后台）
                const topBar = document.querySelector('.bg-primary .flex.gap-4');
                if (topBar) {
                    const existingAdminLinks = topBar.querySelectorAll('a[href*="admin"]');
                    existingAdminLinks.forEach(link => link.remove());
                }
                
                // 只有管理员才显示"管理后台"链接（不区分大小写）
                const isAdmin = user.role && user.role.toUpperCase() === 'ADMIN';
                if (isAdmin) {
                    const topBar = document.querySelector('.bg-primary .flex.gap-4');
                    if (topBar) {
                        const newAdminLink = document.createElement('a');
                        newAdminLink.href = 'admin-dashboard.html';
                        newAdminLink.className = 'hover:underline text-yellow-300';
                        newAdminLink.innerHTML = '<i class="fa fa-shield mr-1"></i> 管理后台';
                        topBar.appendChild(newAdminLink);
                    }
                }
            } else {
                // 用户未登录时，确保移除任何管理员相关的链接
                const topBar = document.querySelector('.bg-primary .flex.gap-4');
                if (topBar) {
                    const existingAdminLinks = topBar.querySelectorAll('a[href*="admin"]');
                    existingAdminLinks.forEach(link => link.remove());
                }
            }
        }

        // 更新桌面端导航栏
        const desktopNav = document.querySelector('header nav.hidden.md\\:flex');
        if (desktopNav && isLoggedIn) {
            // 更新"加入会员"按钮
            const membershipButton = desktopNav.querySelector('a[href*="apply-membership"], a[href*="membership"], button');
            if (membershipButton) {
                const isMember = user.membership && user.membership.toLowerCase() === 'member';
                if (isMember) {
                    // 如果是会员，改为"会员中心"
                    if (membershipButton.tagName === 'A') {
                        membershipButton.textContent = '会员中心';
                        membershipButton.href = '#';
                        membershipButton.onclick = (e) => {
                            e.preventDefault();
                            alert('欢迎回来，会员！会员专属功能正在开发中...');
                        };
                        membershipButton.className = 'bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition';
                    } else {
                        membershipButton.textContent = '会员中心';
                        membershipButton.onclick = (e) => {
                            e.preventDefault();
                            alert('欢迎回来，会员！会员专属功能正在开发中...');
                        };
                        membershipButton.className = 'bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition';
                    }
                } else {
                    // 如果不是会员，保持"加入会员"
                    if (membershipButton.tagName === 'BUTTON') {
                        const link = document.createElement('a');
                        link.href = 'apply-membership.html';
                        link.className = membershipButton.className;
                        link.textContent = '加入会员';
                        membershipButton.replaceWith(link);
                    } else {
                        membershipButton.href = 'apply-membership.html';
                        membershipButton.textContent = '加入会员';
                        membershipButton.className = 'bg-accent text-white px-4 py-2 rounded-md hover:bg-accent/90 transition';
                    }
                }
            }
            
            // 检查是否已经添加了用户菜单，避免重复添加
            if (!desktopNav.querySelector('#user-menu-btn')) {
                // 创建用户下拉菜单（保留"加入会员"按钮）
                const userMenu = document.createElement('div');
                userMenu.className = 'relative';
                
                const isAdmin = user.role && user.role.toUpperCase() === 'ADMIN';
                const isMember = user.membership && user.membership.toLowerCase() === 'member';
                let dropdownContent = `
                    <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                        <i class="fa fa-user mr-2"></i>个人资料
                    </a>
                    <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                        <i class="fa fa-cog mr-2"></i>账户设置
                    </a>
                `;
                
                if (isAdmin) {
                    dropdownContent += `
                    <hr class="my-1">
                    <a href="admin-dashboard.html" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                        <i class="fa fa-shield mr-2"></i>管理后台
                    </a>
                    `;
                }
                
                dropdownContent += `
                    <hr class="my-1">
                    <a href="#" id="logout-btn" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                        <i class="fa fa-sign-out mr-2"></i>退出登录
                    </a>
                `;
                
                userMenu.innerHTML = `
                    <button id="user-menu-btn" class="flex items-center gap-2 bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition">
                        <i class="fa fa-user-circle"></i>
                        <span>${user.username}</span>
                        <i class="fa fa-chevron-down text-xs"></i>
                    </button>
                    <div id="user-dropdown" class="hidden absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50">
                        ${dropdownContent}
                    </div>
                `;
                
                // 在"加入会员"按钮后面添加用户菜单
                desktopNav.appendChild(userMenu);
                
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
            // 更新移动端"加入会员"按钮
            const mobileMembershipButton = mobileMenu.querySelector('button, a[href*="apply-membership"], a[href*="membership"]');
            if (mobileMembershipButton) {
                const isMember = user.membership && user.membership.toLowerCase() === 'member';
                if (isMember) {
                    // 如果是会员，改为"会员中心"
                    if (mobileMembershipButton.tagName === 'A') {
                        mobileMembershipButton.textContent = '会员中心';
                        mobileMembershipButton.href = '#';
                        mobileMembershipButton.onclick = (e) => {
                            e.preventDefault();
                            alert('欢迎回来，会员！会员专属功能正在开发中...');
                        };
                        mobileMembershipButton.className = 'bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition mt-2';
                    } else {
                        mobileMembershipButton.textContent = '会员中心';
                        mobileMembershipButton.onclick = (e) => {
                            e.preventDefault();
                            alert('欢迎回来，会员！会员专属功能正在开发中...');
                        };
                        mobileMembershipButton.className = 'bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 transition mt-2';
                    }
                } else {
                    // 如果不是会员，保持"加入会员"
                    if (mobileMembershipButton.tagName === 'BUTTON') {
                        const link = document.createElement('a');
                        link.href = 'apply-membership.html';
                        link.className = mobileMembershipButton.className;
                        link.textContent = '加入会员';
                        mobileMembershipButton.replaceWith(link);
                    } else {
                        mobileMembershipButton.href = 'apply-membership.html';
                        mobileMembershipButton.textContent = '加入会员';
                        mobileMembershipButton.className = 'bg-accent text-white px-4 py-2 rounded-md hover:bg-accent/90 transition mt-2';
                    }
                }
            }
            
            // 检查是否已经添加了用户信息，避免重复添加
            if (!mobileMenu.querySelector('#mobile-user-info')) {
                const userInfo = document.createElement('div');
                userInfo.id = 'mobile-user-info';
                userInfo.className = 'py-3 border-t border-gray-100';
                
                const isAdmin = user.role && user.role.toUpperCase() === 'ADMIN';
                const isMember = user.membership && user.membership.toLowerCase() === 'member';
                let mobileMenuContent = `
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
                `;
                
                if (isAdmin) {
                    mobileMenuContent += `
                    <a href="admin-dashboard.html" class="block py-2 text-red-600 hover:text-red-700">
                        <i class="fa fa-shield mr-2"></i>管理后台
                    </a>
                    `;
                }
                
                mobileMenuContent += `
                    <button id="mobile-logout-btn" class="w-full mt-2 bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-600 transition">
                        <i class="fa fa-sign-out mr-2"></i>退出登录
                    </button>
                `;
                
                userInfo.innerHTML = mobileMenuContent;
                
                // 在移动端菜单中添加用户信息（保留"加入会员"按钮）
                // 优先添加到 .container 中，如果没有则直接添加到 mobileMenu
                const container = mobileMenu.querySelector('.container');
                if (container) {
                    container.appendChild(userInfo);
                } else {
                    mobileMenu.appendChild(userInfo);
                }
                
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

        const { isLoggedIn, user } = checkAuthStatus();
        const isAdmin = user && user.role && user.role.toUpperCase() === 'ADMIN';

        const menu = document.createElement('div');
        menu.id = 'top-user-menu';
        menu.className = 'absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50';
        menu.style.top = event.target.getBoundingClientRect().bottom + 'px';
        menu.style.right = '1rem';
        
        let menuContent = `
            <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                <i class="fa fa-user mr-2"></i>个人资料
            </a>
            <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                <i class="fa fa-cog mr-2"></i>账户设置
            </a>
        `;
        
        if (isAdmin) {
            menuContent += `
            <hr class="my-1">
            <a href="admin-dashboard.html" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">
                <i class="fa fa-shield mr-2"></i>管理后台
            </a>
            `;
        }
        
        menuContent += `
            <hr class="my-1">
            <a href="#" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100" onclick="event.preventDefault(); if(confirm('确定要退出登录吗?')) { localStorage.clear(); window.location.href='index.html'; }">
                <i class="fa fa-sign-out mr-2"></i>退出登录
            </a>
        `;
        
        menu.innerHTML = menuContent;

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