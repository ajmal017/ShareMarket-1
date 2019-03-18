
var isTesting = false;//vvvvvvvvvvvvvvvvvvvvvvvvvvvIMP, make it to false
var isMISRequired = true;
stopLossPerc=parseFloat(1.5);
var liveDataFull={};
var cash=0;
var blockMsg="BO BLOCKED COMPLETELY";
var blockedFileName="bo_blocked.json";
var totalAmountPlaced = parseFloat(0);
var whenToStartExecute = "09:17:30";
var isPlaceOrderFor3MinStrategy = true;

var dataCollected = [];
var gap = 1.2;
startEntry();
function startEntry()
{
	console.log("starting 3minstrategy");
	var isWait=true;
	var startHour, startMin, startSec;
	startHour = whenToStartExecute.split(":")[0];
	startMin = whenToStartExecute.split(":")[1];
	startSec = whenToStartExecute.split(":")[2];
	while(isWait){
		sleep(1000);
		if(new Date().getHours() >= startHour && new Date().getMinutes() >= startMin && new Date().getSeconds() > startSec)
		{
			isWait = false;
		}
	}
	start();
}
function start()
{
	var invest = parseFloat(getCashInAccount())*marginMultipler*5/100;
	var zerodhaId, res, index, high, low, entryPrice,name, count=1, intradayOpen, isRejected;
	var symbolsFiltered = symbols.filter(a => Math.abs((a[6]-a[5])*100/a[6]) > gap);
	var isDone = false, arrLength=symbolsFiltered.length;
	if(arrLength>100) arrLength = 100;
	var amntToInvestPerSymbol = invest/arrLength;
	for(var i=0; i< arrLength && isPlaceOrderFor3MinStrategy; i++)
	{
		try
		{
			name = symbolsFiltered[i][0];
			zerodhaId = symbolsFiltered[i][1];
			isDone = false;
			while(!isDone){
				index = -1;
				res = getIntradayFromZerodha(zerodhaId);
				if(new Date().getMinutes() <18){
					sleep(2000);
					getPriceFromUpstox(eqExchange,name, 'full');
				}
				var m = new Date().getMinutes(), s = new Date().getSeconds();
				if(res && res.data && res.data.candles)
				{
					if((m==18 && s>=8) || (m>18))
					{
						index = res.data.candles.findIndex(a => a[0].toString().includes("09:15:00"));
					}
				}
				if(m >=18 && s >=8 && index<0){
					isDone=true;
				}
				if(index>=0){
					isDone=true;
				}
			}
			if(index<0){
				continue;
			}
			console.log(name+"-"+new Date());
			console.log(res.data.candles[index]+"---------------------------------------");
			intradayOpen = res.data.candles[index][1];
			high = res.data.candles[index][2]; low = res.data.candles[index][3];
			
			var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
			if(!liveDataFull.ltp){
				sleep(5000);
				console.log("sleeping bcz too many requests 1st time");
				liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
				
				if(!liveDataFull.ltp){
					sleep(5000);
					console.log("sleeping bcz too many requests 2nd time");
					liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
				}
			}
			if(liveDataFull && liveDataFull.total_buy_qty*liveDataFull.ltp > 10000 && liveDataFull.total_sell_qty*liveDataFull.ltp > 10000)
			{
				direction = "BUY";
				triggerObj = getTriggerPrice(liveDataFull, high, low, direction);
				if(triggerObj.NEXT_DIR != undefined){
					direction = "SELL";
					triggerObj = getTriggerPrice(liveDataFull, high, low, "SELL");
				}
				entryPrice = getTickPrice(triggerObj.TRIGGER);
				nextDir = triggerObj.NEXT_DIR;
				squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
				stopLoss = getTickPrice(high-low);
				orderObject = generateOrderObject(name, entryPrice, direction, "SL", boVarietyType, count, intradayOpen,squareOff, stopLoss);
				orderObject.TriggerPrice=entryPrice;
				isRejected = placeZerodhaOrder(orderObject, amntToInvestPerSymbol);
				if(!isRejected && nextDir!=undefined)
				{
					direction = nextDir;
					triggerObj = getTriggerPrice(liveDataFull, high, low, nextDir);
					entryPrice = getTickPrice(triggerObj.TRIGGER);
					squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
					stopLoss = getTickPrice(high-low);
					orderObject = generateOrderObject(name, entryPrice, direction, "SL", boVarietyType, count, intradayOpen,squareOff, stopLoss);
					orderObject.TriggerPrice=entryPrice;
					isRejected = placeZerodhaOrder(orderObject, amntToInvestPerSymbol);
				}
			}	
		}catch(error){
			console.log(error);
		}
	}
	checkAndCancelOtherSideOrder(symbolsFiltered, arrLength);
}

function checkAndCancelOtherSideOrder(symbolsFiltered, arrLength)
{
	var isDone=false;
	while(!isDone && (new Date()).getHours()<= 16)
	{
		console.log("checking to cancel in checkAndCancelOtherSideOrder");
		sleep(30000);
		var res = getAllOrders();
		for(var i=0; i< arrLength; i++)
		{
			try
			{
				name = symbolsFiltered[i][0];
				if(res && res.data){
					var triggerPendingOrders = res.data.filter(obj=> obj.status == "TRIGGER PENDING" && 
					obj.variety=='bo' && obj.parent_order_id==null && obj.tradingsymbol== name);
					if(triggerPendingOrders.length==1)
					{
						triggerPendingOrders.forEach(o => {
							cancelOrder(o.order_id);
						})
					}
				}
			}
			catch(error)
			{}
		}
	}
	console.log("Exiting checkAndCancelOtherSideOrder");
}

function cancelOrder(orderId){
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/bo/"+orderId+"?order_id="+orderId+"&variety=bo";
		
		xhr.open("DELETE", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
	}catch(err){
		console.log(err);
	}
}

function getTriggerPrice(liveDataFull, high, low, direction)
{
	if(liveDataFull.ltp > high && direction=='BUY'){
		return {"TRIGGER": liveDataFull.ltp, "NEXT_DIR": undefined};
	}
	else if(liveDataFull.ltp < low && direction == 'SELL'){
		return {"TRIGGER": liveDataFull.ltp, "NEXT_DIR": undefined};
	}else{
		if(direction=='BUY'){
			return {"TRIGGER": (high + (high*entryPercFromOpen)/100), "NEXT_DIR": "SELL"};
		}else if(direction == 'SELL'){
			return {"TRIGGER": (low - (low*entryPercFromOpen)/100), "NEXT_DIR": "BUY"};
		}
	}
}
function getIntradayFromZerodha(id){
	try{
		var d=getDate(0);
		var xhr = new XMLHttpRequest();
		xhr.open("GET", 'https://kitecharts-aws.zerodha.com/api/chart/'+id+'/3minute?public_token='+csrfToken+'&user_id=DP3137&api_key=kitefront&access_token='+csrfToken+'&from='+d+'&to='+d+'&ciqrandom=1546334949830', false);
		xhr.send();
		var res = JSON.parse(xhr.response);
		return res;
	}catch(error){
		return 0;
	}
}

function getDate(dayToSubstract) {
    var date = new Date();
    date.setDate(date.getDate()-dayToSubstract);
    return date.getFullYear() + '-' + ('0' + (date.getMonth()+1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
}

function getCashInAccount()
{
	cash=0;
	try{
		var xhr = new XMLHttpRequest();
		xhr.open("GET", 'https://kite.zerodha.com/api/user/margins', false);
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		cash = res.data.equity.available.cash;
	}catch(err){
		console.log(err);
		sleep(1000);
		getCashInAccount();
	}finally{
		return cash;
	}
}

function generateOrderObject(company, limitPrice, transaction_type, order_type, variety, count, intradayOpen,squareoff, stoploss)
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
		Count:"",
		IntradayOpen:"",
		SquareOff:"",
		StopLoss:""
	};
	//clearOrder();
	order.Id=company;
	order.TransactionType= transaction_type;
	order.OrderType=order_type;
	order.Quantity=qty;
	order.LimitPrice=limitPrice;// in placeOrder req, for MARKET : 0, LIMIT: limitPrice
	order.TriggerPrice = 0;
	order.Variety=variety;
	order.Count=count;
	order.IntradayOpen=intradayOpen;
	order.SquareOff=squareoff;
	order.StopLoss=stopLoss;
	return order;
}

function getPriceFromUpstox(exchange, name, type){
	liveDataFull={};
	try{
		var xhr = new XMLHttpRequest();
		xhr.open("GET", 'https://api.upstox.com/live/feed/now/'+exchange+'/'+name+'/'+type+'', false);
		xhr.setRequestHeader("authorization", "Bearer "+upstoxAccessToken);
		xhr.setRequestHeader("x-api-key", upstoxApiKey);
		xhr.send();
		var res = JSON.parse(xhr.response);
		
		if(res.data != null && res.data != undefined){
			liveDataFull.ltp=res.data.ltp;
			liveDataFull.total_buy_qty=res.data.total_buy_qty;
			liveDataFull.total_sell_qty=res.data.total_sell_qty;
			liveDataFull.name=name;
			liveDataFull.open=res.data.open;
			liveDataFull.high=res.data.high;
			liveDataFull.low=res.data.low;
			liveDataFull.close=res.data.close;
			liveDataFull.upper_circuit=res.data.upper_circuit;
			liveDataFull.lower_circuit=res.data.lower_circuit;
			if(type=='full'){
				globalResponse=globalResponse+name+"\n";
				if(res.data.asks!=null && res.data.asks != undefined && res.data.asks.length>=2){	globalResponse=globalResponse+JSON.stringify(res.data.asks[0].quantity*res.data.asks[0].price+res.data.asks[1].quantity*res.data.asks[1].price)+"\n";
				}	
				if(res.data.bids!=null && res.data.bids != undefined && res.data.bids.length>=2){		globalResponse=globalResponse+JSON.stringify(res.data.bids[0].quantity*res.data.bids[0].price+res.data.bids[1].quantity*res.data.bids[1].price)+"\n\n";
				}		
			}
		}
	}
	catch(err){
		console.log(err);
		sleep(500);
	}finally{
		return liveDataFull;
	}
}

function placeZerodhaOrder(entryTradeObjects, amntToInvestPerSymbol){
	var isRejected=false;
	amntToInvestPerSymbol = amntToInvestPerSymbol * entryTradeObjects.Count;
	
	if(isPlaceOrder){
		totalAmountPlaced = totalAmountPlaced + parseInt(amntToInvestPerSymbol);
		console.log("Placing orders as Invest="+invest+", placed="+totalAmountPlaced+"");
		if(totalAmountPlaced > invest){
			console.log("Rejecting placing orders as Invest="+invest+", placed="+totalAmountPlaced+"======================");
			return false;
		}
		var orderId = PlaceOrder(entryTradeObjects, amntToInvestPerSymbol);
		
		if(orderId!=0){
			isRejected = isOrderRejected(orderId);
			if(isRejected == blockMsg){
				console.log("Exiting placeZerodhaOrder");
				return blockMsg;
			}
			if(isMISRequired && isRejected=='Trade_Again'){
				entryTradeObjects.OrderType = 'MARKET';
				entryTradeObjects.SquareOff=0;entryTradeObjects.StopLoss=0;entryTradeObjects.Variety="regular";
				console.log("Placing MIS entry order "+entryTradeObjects.TransactionType+" order for symbol="+entryTradeObjects.Id);
				var orderId = PlaceOrder(entryTradeObjects, amntToInvestPerSymbol);
				if(!placeMISExitOrderIfEntryExecuted(entryTradeObjects, orderId))
				{
					if(!placeMISExitOrderIfEntryExecuted(entryTradeObjects, orderId))
					{
						placeMISExitOrderIfEntryExecuted(entryTradeObjects, orderId);
					}
				}
			}
		}
		
	}
	return isRejected;
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

function PlaceOrder(Order, amntToInvestPerSymbol)
{ 
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/"+Order.Variety;
		noOfShares = amntToInvestPerSymbol/Order.LimitPrice;
		if(noOfShares<=1){
			noOfShares=1;
		}
		Order.Quantity = parseInt(noOfShares);
		var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type="+Order.OrderType+"&quantity="+Order.Quantity+"&price="+Order.LimitPrice+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff="+Order.SquareOff+"&stoploss="+Order.StopLoss+"&trailing_stoploss=0&variety="+Order.Variety+"";
		console.log(Order);
		xhr.open("POST", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send(OrderData);
		
		if(!isJsonString(xhr.response)){
			return 0;
		}
		var res = JSON.parse(xhr.response);
		if(res != undefined && res.data == undefined){
			return 0;
		}
		return res.data.order_id;
	}catch(err){
		console.log(err);
		return 0;
	}
}

function isOrderRejected(orderId){
	var msg = "RMS:Blocked for  EQ  nse_cm  BO Remarks: Due to expected volatility, BO and CO will remain blocked.  block type: ALL";
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		var index = res.data.findIndex(obj=>obj.order_id == orderId);
		if(res.data[index] == undefined){
			return false;
		}
		
		if(res.data[index].status == 'REJECTED')
		{
			var statusMsg = res.data[index].status_message;
			var tradingsymbol = res.data[index].tradingsymbol;
			if(statusMsg && statusMsg.includes(msg) || isTesting)
			{
				downloadToFile("BO BLOCKED COMPLETELY" , blockedFileName);
				return blockMsg;
			}
			if(statusMsg && statusMsg.includes('RMS:Blocked') && statusMsg.toLowerCase().includes(tradingsymbol.toLowerCase()))
			{
				return "Trade_Again";
			}
			return true;
		}else{
			return false;
		}
	}catch(err){
		console.log(err);
		return false;
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

function isJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

function getAllOrders()
{
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		return res;
	}catch(err){
		console.log(err);
	}
}

