<%@ page import="java.util.Date" %><% response.addHeader("Cache-Control","no-cache, no-store, must-revalidate"); response.addDateHeader("Last-Modified", (new Date()).getTime()); %><%@ taglib prefix="ww" uri="/webwork" %><ww:property escape="false" value="result"/>