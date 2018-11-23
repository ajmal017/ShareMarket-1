
var typeOfOrder='LIMIT';var varietyType='regular';var entryFromLastPrice=parseFloat(0.01);
function PlaceOrder(Order, variety)
{
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/"+varietyType;
	var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type="+Order.OrderType+"&quantity="+Order.Quantity+"&price="+Order.LimitPrice+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff=0&stoploss=0&trailing_stoploss=0&variety="+variety+"";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
}

function getPositions(){
	var url="https://kite.zerodha.com/api/portfolio/positions";
	var xhr = new XMLHttpRequest();
	xhr.open("GET", url, false);
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.send();
	return JSON.parse(xhr.response);
}

function generateOrderObject(company, limitPrice, transaction_type, order_type, variety, qty)
{
	if(order_type=='MARKET'){
		limitPrice=0;
	}
	var order = {
		Id:"",
		TransactionType:"",
		OrderType:"",
		Quantity:"",
		LimitPrice:"",
		TriggerPrice:"",
		Variety:"",
		Count:""
	};
	//clearOrder();
	order.Id=company;
	order.TransactionType= transaction_type;
	order.OrderType=order_type;
	order.Quantity=qty;
	order.LimitPrice=limitPrice;// in placeOrder req, for MARKET : 0, LIMIT: limitPrice
	order.TriggerPrice = 0;
	order.Variety=variety;
	return order;
}

function getTickPrice(price){
	var tickPrice = 0;
	toAdd = 10-(((parseFloat(price).toFixed(2))*100)%10);
	if(parseInt(toAdd) < 5){
		tickPrice = (((parseFloat(price).toFixed(2))*100)+toAdd)/100;
	}
	if(parseInt(toAdd) >= 5){
		tickPrice = (((parseFloat(price).toFixed(2))*100)+(toAdd-5))/100;
	}
	return tickPrice;
}

function exitOrders()
{
	console.log("started exit");
	var res = getPositions();
	for(var i=0; i<res.data.net.length; i++){
		var obj = res.data.net[i];
		var dir="", quantity=0, last_price=0;
		if(parseInt(obj.quantity)!=0)
		{
			if(parseInt(obj.buy_quantity) > parseInt(obj.sell_quantity)){
				dir="SELL";
				quantity = parseInt(obj.buy_quantity)-parseInt(obj.sell_quantity);
				last_price = obj.last_price-(obj.last_price*entryFromLastPrice)/100;
				last_price = getTickPrice(last_price);
			}else if(parseInt(obj.sell_quantity) > parseInt(obj.buy_quantity)){
				dir="BUY";
				quantity = parseInt(obj.sell_quantity)-parseInt(obj.buy_quantity);
				last_price = obj.last_price+(obj.last_price*entryFromLastPrice)/100;
				last_price = getTickPrice(last_price);
			}
			obj.tradingsymbol = obj.tradingsymbol.replace("&", "%26");
			var exitOrder = generateOrderObject(obj.tradingsymbol, last_price, dir, typeOfOrder, varietyType, quantity);
			PlaceOrder(exitOrder, varietyType);
		}
	}
}

function start(){
	now = new Date();
	millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, parseInt(minute), second, 0) - now;
	if (millisTill10 < 0) {
		millisTill10 += 86400000; 
		console.log("it's already after time");
	}
	setTimeout(exitOrders, millisTill10);
}
start();
