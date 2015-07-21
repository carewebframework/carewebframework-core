zk.$package('cwf.ext');

cwf.ext.Datebox = zk.$extends(zul.db.Datebox, {});

cwf.zk_parseDate = zk.fmt.Date.parseDate;

zk.fmt.Date.parseDate = function() {
	var ms = cwf.parseDate.apply(this, arguments);
    return ms != null ? ms : cwf.zk_parseDate.apply(this, arguments);
};
