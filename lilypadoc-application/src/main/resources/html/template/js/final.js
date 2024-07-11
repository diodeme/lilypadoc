$(document).ready(function () {
    // 获取对象中所有的函数，并执行它们以获取Promise对象
    const promises = $.map(window.methodAsyncBefore, function (func, key) {
        return func(); // 执行函数，假定这些函数返回Promise对象
    });

    // 使用apply()方法将promises数组作为参数传递给$.when()
    $.when.apply($, promises)
    .then(function () {
        for (let identifier in window.methodAfter) {
            if (window.methodAfter.hasOwnProperty(identifier)) {
                window.methodAfter[identifier]() // 调用每个已注册的方法
            }
        }
        setTimeout(() => {
            $('.lilypadoc-view').css('visibility', 'visible')
        }, 10);
    }).catch(function (error) {
        console.log("Error loading content: " + error)
    });
})
