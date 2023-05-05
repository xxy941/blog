$(function(){
    $("#sendBtn").click(send_letter);
});

function send_letter() {
    var toName = $("#recipient-name").val();
    var content = $("#conversation-text").val();
    $.post(
        CONTEXT_PATH + "/conversation/send",
        {"toName":toName,"content":content},
        function () {
                location.reload();
        }
    );
}