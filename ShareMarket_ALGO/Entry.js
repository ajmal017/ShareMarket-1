var entryTradeObjects = [], exitTradeObjects = [], orderIds=[];
var eligibleSymbols = [];
var now = new Date();
var millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, minute, second, 0) - now;
if (millisTill10 < 0) {
    millisTill10 += 86400000; 
	console.log("it's already after 9:15");
}


function collectEligibleSymbol(company, direction, zerodhaId){
	var eligibleSym = {
		Id: company,
		Direction: direction,
		ZerodhaId: zerodhaId,
		Count: 1
	}
	return eligibleSym;
}
function generateOrderObject(company, limitPrice, transaction_type, order_type, variety, count)
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
	order.Count=count;
	return order;
}

function isSymbolAlreadyExist(name, dir){
	var index = parseInt(eligibleSymbols.findIndex(a=> a.Id== name && a.Direction== dir));
	if(index>=0){
		var c = eligibleSymbols[index].Count;
		eligibleSymbols[index].Count = ++c;
		return true;
	}
	return false;
}
//getOnPrevHighLowCompare
function getOnPrevHighLowCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	if((openPrice-yestHigh)*100/openPrice > getOnPrevHighLowCompare1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId);
		}
	}
	else if((yestLow-openPrice)*100/openPrice > getOnPrevHighLowCompare1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId);
		}
	}
}
//gapWithCloseTest
function gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	if((openPrice-yestClose)*100/openPrice > gapWithCloseTest1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && 		openPrice>min && yestClose!=yestOpen){
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId);
		}
	}
	else if((yestClose-openPrice)*100/openPrice > gapWithCloseTest1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && yestClose!=yestOpen){
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId);
		}
	}
}

//getOnPrevCloseCompare
function getOnPrevCloseCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	if((openPrice-yestClose)*100/openPrice > getOnPrevCloseCompare1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && (yestOpen-yestClose)*100/yestClose > getOnPrevCloseCompare2 && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId);
		}
	}
	else if((yestClose-openPrice)*100/openPrice > getOnPrevCloseCompare1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && (yestClose-yestOpen)*100/yestOpen > getOnPrevCloseCompare2 && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId);
		}
	}
}

//getData
function getData(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	if((openPrice-yestClose)*100/openPrice > getData1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		var fib = yestHigh- (yestHigh-yestLow)/2;
		if(openPrice > fib){
			if(!isSymbolAlreadyExist(name, "SELL")){
				eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId);
			}
		}
	}
	else if((yestClose-openPrice)*100/openPrice > getData1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		var fib = yestLow+ (yestHigh-yestLow)/2;
		if(openPrice < fib){
			if(!isSymbolAlreadyExist(name, "BUY")){
				eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId);
			}
		}
	}
}

//getOnPrevCloseCompare
function getOnNiftyPrevCloseGapCheck(name, zerodhaId, openPrice, min, max){
	if((niftyOpen-niftyLastClose)*100/niftyOpen > getOnNiftyPrevCloseGapCheck1 && openPrice < max && openPrice>min && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId);
		}
	}
	else if((niftyLastClose-niftyOpen)*100/niftyOpen > getOnNiftyPrevCloseGapCheck1 && openPrice < max &&  openPrice>min && yestOpen!=yestClose){
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId);
		}
	}
}

function getEligibleSymbols(){
	
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose, fileNo=0;
	var whatPrice = 'O', whichCandle='1', duration = "", liveDataFull;
	var size = Math.floor(symbols.length/1);
	var start = fileNo*size; var end = start+size;
	liveDataFull = getPriceFromUpstox(indexExchange, "nifty_50", 'full');
	niftyOpen = liveDataFull.open;
	liveDataFull = getPriceFromUpstox(indexExchange, "nifty_50", 'full');
	niftyLastClose = liveDataFull.close;
	for(var i=start; i< end; i++){
		name=symbols[i][0]; zerodhaId=symbols[i][1]; yestOpen=parseFloat(symbols[i][2]); yestHigh=parseFloat(symbols[i][3]);
		yestLow=parseFloat(symbols[i][4]);yestClose=parseFloat(symbols[i][5]);
		openPrice=parseFloat(symbols[i][6]);
		if(openPrice==0){
			liveDataFull = getPriceFromUpstox(eqExchange, name, 'full');
			if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 10000 
					|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 10000 ){
				continue;
			}
			openPrice = liveDataFull.open;
		}
		getOnPrevHighLowCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getOnPrevCloseCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getData(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getOnNiftyPrevCloseGapCheck(name, zerodhaId, openPrice, min, max);
	}
	console.log(new Date());
}

function populateTradeObjects(){
	console.log(new Date());
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose;
	var whatPrice = 'ltp', whichCandle='1', duration = 3, total = parseInt(0);
	eligibleSymbols.forEach(a=> total+=a.Count);
	var amntToInvestPerSymbol = invest/total;
	console.log("STARTED");
	for(var i=0; i< eligibleSymbols.length; i++){
		name = eligibleSymbols[i].Id; direction = eligibleSymbols[i].Direction; zerodhaId = eligibleSymbols[i].ZerodhaId, count = eligibleSymbols[i].Count;
		var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
		if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 10000 
				|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 10000 ){
			continue;
		}
		price = liveDataFull.ltp;
		if(direction=='SELL')
		{
			entryPrice = price - (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "SELL", typeOfOrder, varietyType, count);
			placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol);
		}
		else if(direction =='BUY'){
			entryPrice = price + (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "BUY", typeOfOrder, varietyType, count);
			placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol);
		}
	}
//	console.log(globalResponse);
	var blob=new Blob([globalResponse]);
	var link=document.createElement('a');link.href=window.URL.createObjectURL(blob);
	link.download="globalResponse.json";
	sleep(200);
	link.click();
}
function placeZerodhaOrder(entryTradeObjects, amntToInvestPerSymbol){
	var amountToInvestPerSymbol = entryTradeObjects.Count*amntToInvestPerSymbol;
	console.log(entryTradeObjects);
	if(isPlaceOrder){
		PlaceOrder(entryTradeObjects, amountToInvestPerSymbol, varietyType);
	}
}

function startEntry(){
	
	console.log(new Date());
	getEligibleSymbols();
	
	if(eligibleSymbols.length>0){
		populateTradeObjects();
	}
	console.log(new Date());
}

function executedOrders(orderId, symbol){
	var executedOrders = {
		OrderId:"",
		Symbol:"",
		Status: ""
	};
	executedOrders.OrderId=orderId;
	executedOrders.Symbol=symbol;
	executedOrders.Status="";
	return executedOrders;
}

function PlaceOrder(Order, amntToInvestPerSymbol, variety)
{ 
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/"+varietyType;
	noOfShares = amntToInvestPerSymbol/Order.LimitPrice;
	Order.Quantity = parseInt(noOfShares);
	console.log(amntToInvestPerSymbol, Order.Id, Order.Count, Order.LimitPrice*Order.Quantity);
	var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type="+Order.OrderType+"&quantity="+Order.Quantity+"&price="+Order.LimitPrice+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff=0&stoploss=0&trailing_stoploss=0&variety="+variety+"";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
}

function getPriceFromUpstox(exchange, name, type){
	var xhr = new XMLHttpRequest();
	xhr.open("GET", 'https://api.upstox.com/live/feed/now/'+exchange+'/'+name+'/'+type+'', false);
	xhr.setRequestHeader("authorization", "Bearer "+upstoxAccessToken);
	xhr.setRequestHeader("x-api-key", upstoxApiKey);
	xhr.send();
	var res = JSON.parse(xhr.response);
	var liveDataFull={};
	liveDataFull.ltp=res.data.ltp;
	liveDataFull.total_buy_qty=res.data.total_buy_qty;
	liveDataFull.total_sell_qty=res.data.total_sell_qty;
	liveDataFull.name=name;
	liveDataFull.open=res.data.open;
	liveDataFull.close=res.data.close;
	if(type=='full'){
		globalResponse=globalResponse+name+"\n";
		if(res.data.asks!=null && res.data.asks != undefined && res.data.asks.length>=2){	globalResponse=globalResponse+JSON.stringify(res.data.asks[0].quantity*res.data.asks[0].price+res.data.asks[1].quantity*res.data.asks[1].price)+"\n";
		}	
		if(res.data.bids!=null && res.data.bids != undefined && res.data.bids.length>=2){		globalResponse=globalResponse+JSON.stringify(res.data.bids[0].quantity*res.data.bids[0].price+res.data.bids[1].quantity*res.data.bids[1].price)+"\n\n";
		}		
	}
	return liveDataFull;
}

function getPriceFromZerodha(token, duration, whatPrice, whichCandle)
{
	var result="";
	var from=getDate(0), to =getDate(0);
	var getUrl = "https://kitecharts.zerodha.com/api/chart/"+token+"/"+duration+"minute?public_token="+csrfToken+"&user_id=DP3137&api_key=kitefront&access_token="+accessToken+"&from="+from+"&to="+to+"&ciqrandom=1523897795455";
	
	$.ajax({
		statusCode: {
			500: function() {
				console.error("Error while placing order.");
			}
		},
		url : getUrl,
		type: "GET",
		async: false,
		success: function(data, textStatus, jqXHR)
		{		
			if(data != null && data.status == "success"){
				result = getO_H_L_C(data, whatPrice, whichCandle);
			}
		},
		error: function (jqXHR, textStatus, errorThrown)
		{
			console.log("error");
		}
	});
	return result;
}

//get margin from zerodha
function getCashInAccount()
{
	var xhr = new XMLHttpRequest();
	xhr.open("GET", 'https://kite.zerodha.com/api/user/margins', false);
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send();
	var res = JSON.parse(xhr.response);
	result = res.data.equity.available.cash;
	return result;
}

//Utility functions------------------------------------------------------------------------------
function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e7; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}

function getDate(dayToSubstract) {
    var date = new Date();
    date.setDate(date.getDate()-dayToSubstract);
    return date.getFullYear() + '-' + ('0' + (date.getMonth()+1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
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
function getO_H_L_C(data, whatPrice, whichCandle)
{
	var index;
	if(whatPrice=='O') index = 1
	else if(whatPrice=='H') index = 2
	else if(whatPrice=='L') index = 3
	else if(whatPrice=='C') index = 4;
	var price=-999;
	if(data.data.candles.length!=0 && data.data.candles.length>=whichCandle){
		price = data.data.candles[whichCandle][index];
	}
	return price;
}

function isNonNull(value){
	if(value != undefined && value != "" && value != null) return true; else return false;
}


function getPreOpen(){
	var xhr = new XMLHttpRequest();xhr.open("GET", 'https://www.nseindia.com/live_market/dynaContent/live_analysis/pre_open/all.json', false);
	xhr.send();
	var blob=new Blob([xhr.response]);
	var link=document.createElement('a');
	link.href=window.URL.createObjectURL(blob);
	link.download="all.json";link.click();
}

function start(){
	invest = parseFloat(getCashInAccount())*marginMultipler;
	invest = parseFloat(10000);
	now = new Date();
	millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, parseInt(minute), second, 0) - now;
	if (millisTill10 < 0) {
		millisTill10 += 86400000; 
		console.log("it's already after time");
	}
	setTimeout(startEntry, millisTill10);
}

start();
