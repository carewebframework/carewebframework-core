zk.$package('cwf.ext');

cwf.ext.Datebox = zk.$extends(zul.db.Datebox, {});

cwf.zk_parseDate = zk.fmt.Date.parseDate;

zk.fmt.Date.parseDate = function(txt, fmt, strict, refval) {
    return cwf.parseDate(txt);
};

