$(function(){
    $(".follow-btn").click(follow);
});

function follow() {
    var btn = this;
    if($(btn).hasClass("btn-info")) {
        // 关注TA
        $.post(
            CONTEXT_PATH + "/follow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function(data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        //$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        $.post(
            CONTEXT_PATH + "/unfollow",
            {"entityType": 3, "entityId": $(btn).prev().val()},
            function(data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.reload();
                } else {
                    alert(data.msg);
                }
            }
        );
        //$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}

//删除
function setDelete(id,userId) {
    $.post(
        CONTEXT_PATH + "/blog/delete",
        {"id":id,"userId":userId},
        function (data) {
            data = $.parseJSON(data);
            var userid = $("#deleteBtn").val()
            if(data.code == 0){
                location.href = CONTEXT_PATH + "/manager";
            }else {
                alert(data.msg);
            }
        }
    )
}

function update(id,userid) {
    location.href = CONTEXT_PATH + "/blog/editblogpage/" + id + "/" + userid;
}