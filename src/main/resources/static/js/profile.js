$(function(){
	$(".follow-btn").click(follow);
});

function send(loginUserId,userId) {
	if(loginUserId > userId){
		var temp = loginUserId;
		loginUserId = userId;
		userId = temp;
	}
	location.href = CONTEXT_PATH + "/conversation/detail/" + loginUserId + "_" + userId;
}


function follow() {
	var btn = this;
	if($(btn).hasClass("btn")) {
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
				location.href = CONTEXT_PATH + "/user/profile/" + userid;
			}else {
				location.href = CONTEXT_PATH + "/error";
			}
		}
	)
}

//编辑
function update(id,userid) {
	location.href = CONTEXT_PATH + "/blog/editblogpage/" + id + "/" + userid;
}

function enterinfo(userid) {
	location.href = CONTEXT_PATH + "/user/userinfo/" + userid;
}

function enteredit() {
	location.href = CONTEXT_PATH + "/user/editinfo";
}