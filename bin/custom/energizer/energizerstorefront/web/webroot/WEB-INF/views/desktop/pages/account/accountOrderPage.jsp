<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/desktop/order" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
 <jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" /> 

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	<nav:accountNav selected="orders" />
	
	

	<div class="column accountContentPane clearfix">
		<div class="headline">Order Details</div>
		<div class="span-19">
			<div class="span-7 spacing_cls">
				<c:choose>
					<c:when test="${empty orderData.erpOrderCreator}">
						 <spring:theme code="text.account.orderHistory.orderNumber" /> : ${orderData.code}<br />
					</c:when>
					<c:otherwise>
						<spring:theme code="text.account.orderHistory.orderNumber" /> : <p><spring:theme code="text.account.orderHistory.notApplicable" text="Not Applicable" /></p></br>
					</c:otherwise>
				 </c:choose>
				
				<c:if test="${not empty orderData.erpOrderNumber}">
				<spring:theme code="text.account.orderHistory.SAPorderNumber" /> : ${orderData.erpOrderNumber}<br />
				</c:if>
				<spring:theme code="text.account.orderHistory.orderPlaced" /> : 
				<c:if test="${not empty orderData.created}">
					${DateTimeUtil.displayDate(orderData.created)}
				</c:if>
				<br />
				<c:if test="${not empty orderData.statusDisplay}">
					<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.orderStatus" /> : <spring:theme code="text.account.order.status.display.${orderData.status}" /></p>
					<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.poNumber" /> : ${orderData.purchaseOrderNumber}</p>
				</c:if>	
				<c:choose>
					<c:when test="${empty orderData.erpOrderCreator}">
					<p class="reduce_space_cls"> <spring:theme code="Order Type" /> : <spring:theme code="text.account.order.portal" /></p>
					</c:when>
					<c:otherwise>
					 <p class="reduce_space_cls"><spring:theme code="Order Type" /> :<spring:theme code="text.account.order.offline" /></p>
					</c:otherwise>
				 </c:choose>
			</div>	
	
			<div class="span-5">&nbsp;
				<order:receivedPromotions order="${orderData}"/>
			</div>
			
			<c:if test="${orderData.triggerData ne null}">
				<order:replenishmentScheduleInformation order="${orderData}"/>
			</c:if>
			
			<div class="span-6 last order-totals">
				<order:orderTotalsItem order="${orderData}"/>
			</div>
			
			<div class="span-19 last orderFix-cls">
			<sec:authorize ifAnyGranted="ROLE_B2BCUSTOMERGROUP,ROLE_B2BADMINGROUP">
		  		 <order:reorderButton order="${orderData}"/>
		  	</sec:authorize>	
				 <c:if test="${not empty orderData.status && orderData.status == 'INVOICED' }"> 
				 	<order:viewInvoiceButton orderData="${orderData}"/>
				</c:if> 
			</div>
			
		</div>
		
		
		<div class="orderBoxes clearfix">
			<order:deliveryAddressItem order="${orderData}"/>
			<order:orderShippingDetails order="${orderData}"/>
			<!-- display only if credit card payment -->
			<c:if test="${orderData.paymentType.code.equals(CheckoutPaymentType.CARD.getCode()) }">
				<div class="orderBox billing">
					<order:billingAddressItem order="${orderData}"/>
				</div>
			</c:if>
			<c:if test="${not empty orderData.paymentInfo}">
				<div class="orderBox payment">
					<order:paymentDetailsItem order="${orderData}"/>
				</div>
			</c:if>
		</div>
	
		<c:if test="${not empty orderData.b2bPermissionResult}">
			<order:orderApprovalDetailsItem order="${orderData}" />
		</c:if> 

	    <order:energizerOrderDetailsItem order="${orderData}"/> 

	</div>
</template:page>