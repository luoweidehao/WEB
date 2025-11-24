/**
 * 会员申请登录检查脚本
 * 检查用户是否登录，如果未登录则显示提示框
 */

(function() {
    'use strict';

    // 检查用户是否登录
    function isUserLoggedIn() {
        const token = localStorage.getItem('token');
        const userStr = localStorage.getItem('user');
        return !!(token && userStr);
    }

    // 显示登录提示模态框
    function showLoginPrompt() {
        // 检查是否已经存在模态框
        const existingModal = document.getElementById('login-prompt-modal');
        if (existingModal) {
            existingModal.classList.remove('hidden');
            return;
        }

        // 创建模态框
        const modal = document.createElement('div');
        modal.id = 'login-prompt-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4';
        modal.innerHTML = `
            <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
                <div class="text-center mb-6">
                    <div class="inline-flex items-center justify-center w-16 h-16 bg-yellow-100 rounded-full mb-4">
                        <i class="fa fa-exclamation-triangle text-yellow-600 text-2xl"></i>
                    </div>
                    <h3 class="text-xl font-bold text-gray-800 mb-2">需要登录</h3>
                    <p class="text-gray-600">请先登录后再申请加入会员</p>
                </div>
                <div class="flex gap-3">
                    <button id="go-to-login-btn" class="flex-1 bg-primary text-white px-4 py-2 rounded-md hover:bg-secondary transition">
                        <i class="fa fa-sign-in mr-2"></i>前往登录
                    </button>
                    <button id="cancel-btn" class="flex-1 bg-gray-200 text-gray-700 px-4 py-2 rounded-md hover:bg-gray-300 transition">
                        <i class="fa fa-times mr-2"></i>取消
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // 绑定事件
        document.getElementById('go-to-login-btn').addEventListener('click', () => {
            window.location.href = 'login.html';
        });

        document.getElementById('cancel-btn').addEventListener('click', () => {
            modal.classList.add('hidden');
        });

        // 点击模态框外部关闭
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.add('hidden');
            }
        });
    }

    // 查找包含"加入会员"文本的按钮或链接元素
    function findMembershipElement(element) {
        let current = element;
        while (current && current !== document.body) {
            if (current.tagName === 'A' || current.tagName === 'BUTTON') {
                const text = current.textContent.trim();
                if (text.includes('加入会员') || text.includes('立即加入')) {
                    return current;
                }
            }
            current = current.parentElement;
        }
        return null;
    }

    // 处理"加入会员"按钮点击（使用事件委托）
    function handleMembershipButtonClick(e, target) {
        const text = target.textContent.trim();
        const href = target.getAttribute('href');
        
        // 清除可能被其他脚本设置的onclick处理器
        if (target.onclick) {
            target.onclick = null;
        }
        
        if (!isUserLoggedIn()) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            showLoginPrompt();
            return false;
        }
        
        // 如果已登录，对于按钮需要手动跳转
        if (target.tagName === 'BUTTON') {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            window.location.href = 'apply-membership.html';
            return false;
        }
        
        // 如果已登录且是链接，检查是否需要跳转
        if (target.tagName === 'A' && (!href || href === '#')) {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            window.location.href = 'apply-membership.html';
            return false;
        }
        
        // 如果已登录且是有效链接，允许正常跳转
        return true;
    }

    // 初始化：使用事件委托，监听整个文档的点击事件
    function init() {
        // 移除可能已存在的事件监听器（避免重复绑定）
        if (window.membershipCheckHandler) {
            document.removeEventListener('click', window.membershipCheckHandler, true);
        }
        
        // 创建事件处理函数
        window.membershipCheckHandler = function(e) {
            // 查找包含"加入会员"的元素
            const target = findMembershipElement(e.target);
            if (target) {
                // 阻止其他事件处理器（包括auth-navbar.js设置的onclick）
                e.stopImmediatePropagation();
                handleMembershipButtonClick(e, target);
            }
        };
        
        // 使用捕获阶段，确保能拦截到事件（在其他脚本之前）
        // 使用 {capture: true, passive: false} 确保可以阻止默认行为
        document.addEventListener('click', window.membershipCheckHandler, {capture: true, passive: false});
    }

    // 立即初始化（不等待DOM加载完成，因为使用事件委托）
    init();
    
    // 页面加载完成后再次初始化（确保覆盖动态添加的元素）
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            init();
            // 延迟执行，确保auth-navbar.js已经运行
            setTimeout(init, 200);
        });
    } else {
        // 延迟执行，确保其他脚本已经运行
        setTimeout(init, 200);
    }
    
    // 监听DOM变化，如果按钮被动态替换，重新绑定
    if (window.MutationObserver) {
        const observer = new MutationObserver(function(mutations) {
            // 检查是否有按钮被添加或修改
            let shouldReinit = false;
            mutations.forEach(function(mutation) {
                if (mutation.type === 'childList' || mutation.type === 'attributes') {
                    const nodes = mutation.addedNodes || [];
                    for (let i = 0; i < nodes.length; i++) {
                        const node = nodes[i];
                        if (node.nodeType === 1) { // Element node
                            if (node.tagName === 'BUTTON' || node.tagName === 'A') {
                                const text = node.textContent || '';
                                if (text.includes('加入会员')) {
                                    shouldReinit = true;
                                    break;
                                }
                            }
                            // 检查子元素
                            if (node.querySelector && node.querySelector('button, a')) {
                                const buttons = node.querySelectorAll('button, a');
                                buttons.forEach(function(btn) {
                                    if (btn.textContent && btn.textContent.includes('加入会员')) {
                                        shouldReinit = true;
                                    }
                                });
                            }
                        }
                    }
                }
            });
            if (shouldReinit) {
                setTimeout(init, 50);
            }
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true,
            attributes: true,
            attributeFilter: ['onclick', 'href']
        });
    }
})();

