/** --------------- table复选框选择功能js ---------------- start */
//表头复选框 全选/取消全选 方法
function checkPageSelected_(obj) {
    var objs = document.getElementsByName(obj.name);
    var checked = obj.checked;
    for (var i = 0; i < objs.length; i++) {
        objs[i].checked = checked;
    }
}

//表头复选框 选中/取消 触发方法
function checkRowSelected_(obj) {
    var objs = document.getElementsByName(obj.name);
    if (!obj.checked) {
        document.getElementById(obj.name + 'head').checked = '';
        return;
    } else {
        document.getElementById(obj.name + 'head').checked = 'checked';
    }
    for (var i = 0; i < objs.length; i++) {
        if (!objs[i].checked && objs[i] != obj) {
            document.getElementById(obj.name + 'head').checked = '';
            break;
        }
    }
}

//加载行id
var idstr = [];
var idmap = "";

function loadcheck(obj, id) {
    if (!obj.checked) {
        deleteid(idstr, id);
    } else {
        idstr.push(id);
    }
}

//加载全部id
function loadallids(obj, datalist) {
    idstr = [];
    if (obj.checked) {
        datalist.forEach(function (rowdata, index, array) {
            idstr.push(rowdata.id);
        });
    }
}

//加载全部部分id，根据元素排除值val
function loadpartids(obj, datalist, field, val) {
    idstr = [];
    if (obj.checked) {
        datalist.forEach(function (rowdata, index, array) {
            if (rowdata[field] != val) {
                idstr.push(rowdata.id);
            }
        });
    }
}

//获取选择框已勾选的id列表字符串
function getidmap() {
    for (var i = 0; i < idstr.length; i++) {
        if (i == 0) {
            idmap = idstr[0] + ":" + idstr[0];
        } else {
            idmap = idmap + "," + idstr[i] + ":" + idstr[i];
        }
    }
    return idmap;
}

//获取指定table的id字符串全集（例：1,2,3,4）
function getAllidstr(tobj) {
    var target = "";
    //获取表数据
    var tdata = tobj.bootstrapTable('getData');
    //遍历行数据index:行号(从0开始)；rowdata:行数据对象
    $.each(tdata, function (index, rowdata) {
        if (index == 0) {
            target = rowdata.id;
        } else {
            target += "," + rowdata.id;
        }
    })
    return target;
}

//获取指定table中已勾选项的id字符串全集（例：1,3,4）
function getSelectidstr(tobj) {
    var target = "";
    //获取表数据
    var tdata = tobj.bootstrapTable('getSelections');
    //遍历行数据index:行号(从0开始)；rowdata:行数据对象
    $.each(tdata, function (index, rowdata) {
        if (index == 0) {
            target = rowdata.id;
        } else {
            target += "," + rowdata.id;
        }
    })
    return target;
}

//删除指定table的已勾选行，页面操作不到后台
function delCheckRow(tobj) {
    //获取选中数据
    var rows = tobj.bootstrapTable('getSelections');
    if (rows.length == 0) {
        alert("请先勾选!");
        return;
    } else {
        //遍历行数据index:行号(从0开始)；rowdata:行数据对象
        $.each(rows, function (index, rowdata) {
            tobj.bootstrapTable('remove', {
                field: 'index',
                values: [rowdata.index]
            });
        });
    }
}

//获取指定table中指定列的字符串全集
function getAllstrByField(tobj, field) {
    var target = "";
    //获取表数据
    var tdata = tobj.bootstrapTable('getData');
    //遍历行数据index:行号(从0开始)；rowdata:行数据对象
    $.each(tdata, function (index, rowdata) {
        if (index == 0) {
            target = $(rowdata[field]).val();
        } else {
            target += "," + $(rowdata[field]).val();
        }
    });
    return target;
}

//根据id获取指定table中指定列的指定子元素的字符串全集
function getAllInputstrById(tobj, idhead) {
    var target = "";
    //获取表数据
    var tdata = tobj.bootstrapTable('getData');
    //遍历行数据index:行号(从0开始)；rowdata:行数据对象
    $.each(tdata, function (index, rowdata) {
        if (index == 0) {
            target = $('#' + idhead + rowdata.index).val();
        } else {
            target += "," + $('#' + idhead + rowdata.index).val();
        }
    });
    return target;
}

//获取指定表格index最大值
function getMaxIndex(tobj) {
    var target = 0;
    //获取表数据
    var tdata = tobj.bootstrapTable('getData');
    //遍历行数据index:行号(从0开始)；rowdata:行数据对象
    $.each(tdata, function (index, rowdata) {
        if (rowdata.index > target) {
            target = rowdata.index;
        }
    });
    return target;
}

//获取指定表行数
function getRowNum(tobj) {
    var tdata = tobj.bootstrapTable('getData');
    return tdata.length;
}

//按id勾选
function checkByIndexs(tobj, ids) {
    if (ids != null && tobj != null) {
        //获取表数据
        var tdata = tobj.bootstrapTable('getData');
        //遍历行数据index:行号(从0开始)；rowdata:行数据对象
        $.each(tdata, function (index, rowdata) {
            //判断rowdata.id是否包含
            var idmap = ids.toString().split(",");
            if (idmap.indexOf(rowdata.id.toString()) != -1) {
                tobj.bootstrapTable('check', index);
            } else {
                tobj.bootstrapTable('uncheck', index);
            }
        });
    }

}

/** -------------- table表头复选框全选功能js -------------- end */


/** input框修改内容更改value */
function changeInputValue(obj) {
    $(obj).attr("value", $(obj).val());
}

/** 获取某元素下所有input的值 */
function getAllInputStr(obj) {
    var target = "";
    $(obj).each(function () {
        $(this).find("input").each(function () {
            target += "," + $(this).val();
        })
    })
    return target;
}

/** 判断是否为实数 */
function isShiShu(str) {
    var reg = /^\d+(\.{0,1}\d+){0,1}$/;
    return reg.test(str);
}

/** 判断是否为整数 */
function isNumber(str) {
    var reg = /^\d+$/;
    return reg.test(str);
}

/** 跳转到指定url */
function gotourl(url) {
    document.location.href = url;
}

/** 是纯数字的话返回true 否则返回false */
function isNum(obj) {
    var reg = /^[0-9]*$/;
    return reg.test(obj);
}

/** 删除数组指定元素 */
function deleteid(arr, i) {
    var index = arr.indexOf(i);
    arr.splice(index, 1)
}

/** 判断是否IE浏览器 */
function isIE() {
    if (!!window.ActiveXObject || "ActiveXObject" in window) {
        return true;
    } else {
        return false;
    }
}

/** 判断IE浏览器的具体版本 */
function IEVersion() {
    var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1; //判断是否IE<11浏览器
    var isEdge = userAgent.indexOf("Edge") > -1 && !isIE; //判断是否IE的Edge浏览器
    var isIE11 = userAgent.indexOf('Trident') > -1 && userAgent.indexOf("rv:11.0") > -1;
    if (isIE) {
        var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
        reIE.test(userAgent);
        var fIEVersion = parseFloat(RegExp["$1"]);
        if (fIEVersion == 7) {
            return 7;
        } else if (fIEVersion == 8) {
            return 8;
        } else if (fIEVersion == 9) {
            return 9;
        } else if (fIEVersion == 10) {
            return 10;
        } else {
            return 6;//IE版本<=7
        }
    } else if (isEdge) {
        return 'edge';//edge
    } else if (isIE11) {
        return 11; //IE11
    } else {
        return -1;//不是ie浏览器
    }
}

//格式化日期-时间
// 例子：(new Date()).Format("yyyy-MM-dd HH:mm:ss.S") ==> 2006-07-02 08:09:04.42
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

/**
 * 对日期进行格式化，
 * @param date 要格式化的日期
 * @param format 进行格式化的模式字符串
 *     支持的模式字母有：
 *     y:年,
 *     M:年中的月份(1-12),
 *     d:月份中的天(1-31),
 *     h:小时(0-23),
 *     m:分(0-59),
 *     s:秒(0-59),
 *     S:毫秒(0-999),
 *     q:季度(1-4)
 * @return String
 */
// 兼容ie
function dateFormat(date, format) {
    var time;
    if (!date) return "";
    if (typeof date === 'string') {
        time = new Date(date.replace(/-/g, '/').replace(/T|Z/g, ' ').trim());
    } else if (typeof date === 'object') {
        time = new Date(date);
    }
    var o = {
        "M+": time.getMonth() + 1, //月份
        "d+": time.getDate(), //日
        "h+": time.getHours(), //小时
        "m+": time.getMinutes(), //分
        "s+": time.getSeconds(), //秒
        "q+": Math.floor((time.getMonth() + 3) / 3), //季度
        "S": time.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(format)) format = format.replace(RegExp.$1, (time.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(format)) format = format.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return format;
}

/** 获取系统版本信息 */
function getSysVersion() {
    $.ajax({
        type: "GET"
        , url: "/DMIL/WebService/version/getversion"
        , data: {}
        , contentType: "application/x-www-form-urlencoded;charset=utf-8;"
        , dataType: "json"
        , async: true
        , cache: false
        , success: function (data) {
            if (data.result == 0) {
                console.log("版本信息获取成功!");
                version = data.version;
                $("#sys_version").text(version.version);
                $("#sys_update").text(version.update);
            } else {
                alert(data.message);
            }
        }
        , error: function (XMLHttpRequest, textStatus, errorThrown) {
            //alert(XMLHttpRequest.status + textStatus);
        }
    });
}

