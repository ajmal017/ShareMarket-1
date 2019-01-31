/**if(variety=='bo'){
	squareoff=0;
	stopLoss=0;
}**/

/**var stopLossPerc=parseFloat(3);
var boVarietyType='bo';
var varietyType='bo';
for testing make sure invest var
remove placeStopLossOrders();**/

var entryFromLastPrice=parseFloat(0.01);
function getBOOpenOrder(symbol, dir)
{
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		if(res.data){
			return res.data.filter(obj=> obj.tradingsymbol == symbol && obj.status == "OPEN" && obj.transaction_type == dir && obj.variety=='bo');
		}
		return null;
	}catch(err){
		console.log(err);
	}
}
	
function PlaceOrder(Order, last_price)
{
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/bo/"+Order.order_id;
		var OrderData = "exchange=NSE&tradingsymbol="+Order.tradingsymbol+"&transaction_type="+Order.transaction_type+"&order_type=LIMIT&quantity="+Order.quantity+"&price="+last_price+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price=0&squareoff=0&stoploss=0&trailing_stoploss=0&variety=bo&user_id=DP3137&order_id="+Order.order_id+"&parent_order_id=";
		
		xhr.open("PUT", url, true);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send(OrderData);
	}catch(err){
		console.log(err);
	}
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

function getPositions(){
	try{
		var url="https://kite.zerodha.com/api/portfolio/positions";
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, false);
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.send();
		return JSON.parse(xhr.response);
	}catch(err){
		console.log(err);
		getPositions();
	}
}

function isExitCompleted(){
	console.log("started exit");
	var res = getPositions();
	var boOrder="";
	
	for(var i=0; i<res.data.net.length; i++){
		var obj = res.data.net[i];
		if(obj.product == 'BO')
		{
			var dir="", quantity=0, last_price=0;
			if(parseInt(obj.quantity)!=0)
			{
				if(parseInt(obj.buy_quantity) > parseInt(obj.sell_quantity)){
					dir="SELL";
					last_price = obj.last_price-(obj.last_price*entryFromLastPrice)/100;
					last_price = getTickPrice(last_price);
					boOrder = getBOOpenOrder(obj.tradingsymbol, dir);
				}else if(parseInt(obj.sell_quantity) > parseInt(obj.buy_quantity)){
					dir="BUY";
					last_price = obj.last_price+(obj.last_price*entryFromLastPrice)/100;
					last_price = getTickPrice(last_price);
					boOrder = getBOOpenOrder(obj.tradingsymbol, dir);
				}
				obj.tradingsymbol = obj.tradingsymbol.replace("&", "%26");
				if(boOrder){
					for(var k=0; k< boOrder.length; k++){
						PlaceOrder(boOrder[k], last_price);
					}
				}
			}
		}	
	}
	if(res.data.net.filter(a => a.quantity!=0 && a.product == 'BO').length >0){
		return false;
	}else{
		return true;
	}
}

function exitOrders()
{
	var isExitedAll = false;
	while(!isExitedAll){
		isExitedAll = isExitCompleted();
		sleep(10000);
	}
}

function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e30; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
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
