window.methodAsyncBefore = {}
window.methodAfter = {}

function loadElement (selector, url) {
    return new Promise((resolve, reject) => {
        $(selector).load(url, function (responseText, textStatus) {
            if (textStatus === 'success') {
                resolve(responseText) // 加载成功，解析Promise
            } else {
                reject('Failed to load ' + url) // 加载失败，拒绝Promise
            }
        })
    })
}