$(function(){
	$(".follow-btn").click(follow);
});

function send(loginUserId,userId) {
	if(loginUserId > userId){
		var temp = loginUserId;
		loginUserId = userId;
		userId = temp;
	}
	location.href = CONTEXT_PATH + "/letter/detail/" + loginUserId + "_" + userId;
}


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
function setDelete(id) {
	$.post(
		CONTEXT_PATH + "/blog/delete",
		{"id":id},
		function (data) {
			data = $.parseJSON(data);
			var userid = $("#deleteBtn").val()
			if(data.code == 0){
				location.href = CONTEXT_PATH + "/user/profile/" + userid;
			}else {
				alert(data.msg);
			}
		}
	)
}