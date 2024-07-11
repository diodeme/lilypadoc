let lastScrollTop = 0

$(window).scroll(function () {
    let currentScroll = $(this).scrollTop()

    if (currentScroll > lastScrollTop) {
        // 向下滚动
        $('#header').css('top', -$('#header').outerHeight() + 'px')
    } else {
        // 向上滚动
        $('#header').css('top', 0)
    }

    lastScrollTop = currentScroll
})

// 监听文档的点击事件
$(document).on('click', function (event) {
    // 确保点击是在 <details> 以外的区域
    if (!$(event.target).closest('#header details').length) {
        // 如果不是，则关闭所有 <details>
        $('#header details[open]').removeAttr('open')
    }
})

// 阻止在 <details> 内部的点击事件传播
$('#header details').on('click', function (event) {
    event.stopPropagation()
})