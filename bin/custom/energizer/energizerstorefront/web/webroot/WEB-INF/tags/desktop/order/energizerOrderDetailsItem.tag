<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/desktop/order"%>

<div class="orderList">
	<div class="headline"><spring:theme code="basket.page.title.yourDeliveryItems" text="Your Delivery Items"/></div>
	
	<table class="orderListTable" border="1">
		<thead>
			<tr>
				<th id="header2" colspan="2"><spring:theme code="text.productDetails" text="Product Details"/></th>
				<th id="header14"><spring:theme code="basket.page.prdCode" text="Material ID"/></th>
				<th id="header15"><spring:theme code="basket.page.cmirId" text="CMIR ID"/></th>
				<th id="header4"><spring:theme code="text.quantity" text="Quantity"/></th>

				<th id="header5"><spring:theme code="basket.page.unitPrice" text="Item Price"/></th>
				<th id="header6"><spring:theme code="text.total" text="Total"/></th>
				<th id="header7"><spring:theme code="text.adjustedquantity" text="Adjusted Quantity"/></th>
				<th id="header8"><spring:theme code="text.adjustedprice" text="Adjusted Price"/></th>
				<th id="header9"><spring:theme code="text.adjustedtotal" text="Adjusted Total"/></th>
				<th id="header10"><spring:theme code="text.rejectedStatus" text="Rejected"/></th>
				
				
			<%--<th id="header16"><spring:theme code="text.account.Customer.Product.Name" text="Customer Product Name"/></th>
				<th id="header17"><spring:theme code="text.account.Customer.Specific.Price" text="Customer Specific Price"/></th>
				<th id="header18"><spring:theme code="basket.page.shipFrom" text="Ship From"/></th>
				<th id="header19"><spring:theme code="text.account.MOQ" text="MOQ"/></th>
				<th id="header20"><spring:theme code="text.account.excelUpload.uom" text="UOM"/></th>
				 <th id="header21"><spring:theme code="text.account.Shipped.Quantity" text="Shipped Quantity"/></th> --%>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${order.entries}" var="entry">
			<c:if test="${empty entry.entries}" >
					<order:orderEntryDetail order="${order}" entry="${entry}"/>
			</c:if>	
			
			<c:if test="${not empty entry.entries}" >
				<c:forEach items="${entry.entries}" var="subEntry">
					 <order:orderEntryDetail order="${order}" entry="${subEntry}"/> 
				</c:forEach>
			</c:if>	
		</c:forEach>
		</tbody>
	</table>

</div>
