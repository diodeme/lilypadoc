window.methodAsyncBefore['indexHeader'] = function() {
    return loadElement("#header", "/template/template_after_fusion.html #header > *");
};

window.methodAsyncBefore['indexFooter'] = function() {
    return loadElement("#footer", "/template/template_after_fusion.html #footer > *");
};