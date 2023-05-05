

function like(btn,entityType,entityId,entityUserId){
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("span").text(data.likeCount);
            } else {
                alert(data.msg);
            }
        }
    )
}

