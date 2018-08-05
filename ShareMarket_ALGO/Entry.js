
var entryTradeObjects = [],  exitTradeObjects = [], orderIds=[];
var eligibleSymbols = []; var zeroQtySymbols = [];
var now = new Date();
var millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, minute, second, 0) - now;
if (millisTill10 < 0) {
    millisTill10 += 86400000; 
	console.log("it's already after 9:15");
}


function collectEligibleSymbol(company, direction, zerodhaId, openPrice){
	var eligibleSym = {
		Id: company,
		Direction: direction,
		ZerodhaId: zerodhaId,
		Count: 1,
		IntradayOpen: openPrice
	}
	return eligibleSym;
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
	if(variety=='bo'){
		squareoff=0;
		stopLoss=0;
	}
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

function isSymbolAlreadyExist(name, dir){
	var index = parseInt(eligibleSymbols.findIndex(a=> a.Id== name && a.Direction== dir));
	if(index>=0){
		var c = eligibleSymbols[index].Count;
		eligibleSymbols[index].Count = ++c;
		return true;
	}
	return false;
}

function isIndexStrategy(strategyType, entryType,openPrice, yestOpen, yestHigh, yestLow, yestClose){
	if(isIndexCheck){
		if(strategyType == 'getOnPrevHighLowCompare'){
			if(entryType == 'SELL'){
				return (niftyData.Open-niftyData.PrevHigh)*100/niftyData.Open > getOnPrevHighLowCompare1/indexGapDiv;
			}else if (entryType == 'BUY'){
				return (niftyData.PrevLow-niftyData.Open)*100/niftyData.Open > getOnPrevHighLowCompare1/indexGapDiv;
			}
		}else if(strategyType == 'gapWithCloseTest'){
			if(entryType == 'SELL'){
				return (niftyData.Open-niftyData.PrevClose)*100/niftyData.Open > gapWithCloseTest1/indexGapDiv;
			}else if (entryType == 'BUY'){
				return (niftyData.PrevClose-niftyData.Open)*100/niftyData.Open > gapWithCloseTest1/indexGapDiv;
			}
		}else if (strategyType == 'getOnPrevCloseCompare'){
			if(entryType == 'SELL'){
				return (niftyData.Open-niftyData.PrevClose)*100/niftyData.Open > getOnPrevCloseCompare1/indexGapDiv;
			}else if (entryType == 'BUY'){
				return (niftyData.PrevClose-niftyData.Open)*100/niftyData.Open > getOnPrevCloseCompare1/indexGapDiv;
			}
		}else if (strategyType == 'getData'){
			if(entryType == 'SELL'){
				var prevHighLowRange = (niftyData.PrevHigh - niftyData.PrevLow)/2;
				var fib = niftyData.PrevHigh - prevHighLowRange;
				return niftyData.Open > fib;
			}else if (entryType == 'BUY'){
				var prevHighLowRange = (niftyData.PrevHigh - niftyData.PrevLow)/2;
				var fib = niftyData.PrevLow + prevHighLowRange;
				return niftyData.Open < fib;
			}
		}else if (strategyType == 'getOnNiftyPrevCloseGapCheck'){
			if(entryType == 'SELL'){
				return openPrice>yestHigh;
			}else if (entryType == 'BUY'){
				return openPrice<yestLow;
			}
		}
	}
	return false;
}

function checkAndAddIndexStrategySymbol(strategyType, entryType, name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose){
	if(isIndexStrategy(strategyType, entryType,openPrice, yestOpen, yestHigh, yestLow, yestClose)){
		console.log("index="+strategyType+","+name+","+entryType);
		if(!isSymbolAlreadyExist(name, entryType)){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, entryType, zerodhaId, openPrice);
		}
	}
}
//getOnPrevHighLowCompare
function getOnPrevHighLowCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	var strategyType = 'getOnPrevHighLowCompare';
	if((openPrice-yestHigh)*100/openPrice > getOnPrevHighLowCompare1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice);
		}
	}
	else if((yestLow-openPrice)*100/openPrice > getOnPrevHighLowCompare1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice);
		}
	}
}
//gapWithCloseTest
function gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	var strategyType = 'gapWithCloseTest';
	if((openPrice-yestClose)*100/openPrice > gapWithCloseTest1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && 		openPrice>min && yestClose!=yestOpen){
		checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice);
		}
	}
	else if((yestClose-openPrice)*100/openPrice > gapWithCloseTest1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && yestClose!=yestOpen){
		checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice);
		}
	}
}

//getOnPrevCloseCompare
function getOnPrevCloseCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	var strategyType = 'getOnPrevCloseCompare';
	if((openPrice-yestClose)*100/openPrice > getOnPrevCloseCompare1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && (yestOpen-yestClose)*100/yestClose > getOnPrevCloseCompare2 && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice);
		}
	}
	else if((yestClose-openPrice)*100/openPrice > getOnPrevCloseCompare1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max &&  openPrice>min && (yestClose-yestOpen)*100/yestOpen > getOnPrevCloseCompare2 && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice);
		}
	}
}

//getData
function getData(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){
	var strategyType = 'getData';
	if((openPrice-yestClose)*100/openPrice > getData1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		var fib = yestHigh- (yestHigh-yestLow)/2;
		if(openPrice > fib){
			checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
			if(!isSymbolAlreadyExist(name, "SELL")){
				eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice);
			}
		}
	}
	else if((yestClose-openPrice)*100/openPrice > getData1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max && openPrice>min && yestOpen!=yestClose){
		var fib = yestLow+ (yestHigh-yestLow)/2;
		if(openPrice < fib){
			checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
			if(!isSymbolAlreadyExist(name, "BUY")){
				eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice);
			}
		}
	}
}

//getOnNiftyPrevCloseGapCheck
function getOnNiftyPrevCloseGapCheck(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max){

	var strategyType = 'getOnNiftyPrevCloseGapCheck';
	if((niftyOpen-niftyLastClose)*100/niftyOpen > getOnNiftyPrevCloseGapCheck1 && openPrice < max && openPrice>min && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "SELL")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice);
		}
	}
	else if((niftyLastClose-niftyOpen)*100/niftyOpen > getOnNiftyPrevCloseGapCheck1 && openPrice < max &&  openPrice>min && yestOpen!=yestClose){
		checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose);
		if(!isSymbolAlreadyExist(name, "BUY")){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice);
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
	niftyData.Open = niftyOpen; //Collected nifty open
	liveDataFull = getPriceFromUpstox(indexExchange, "nifty_50", 'full');
	niftyLastClose = liveDataFull.close;
	for(var i=start; i< end; i++){
		name=symbols[i][0]; zerodhaId=symbols[i][1]; yestOpen=parseFloat(symbols[i][2]); yestHigh=parseFloat(symbols[i][3]);
		yestLow=parseFloat(symbols[i][4]);yestClose=parseFloat(symbols[i][5]);
		openPrice=parseFloat(symbols[i][6]);
		
		liveDataFull = getPriceFromUpstox(eqExchange, name, 'full');
		if(liveDataFull.ltp==undefined || liveDataFull.ltp==null){
			continue;
		}
		if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 1000 
				|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 1000 ){
			continue;
		}
		openPrice = liveDataFull.open;
		
		getOnPrevHighLowCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getOnPrevCloseCompare(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getData(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
		getOnNiftyPrevCloseGapCheck(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max);
	}
}

function executeDummyOrder(whichTab, eligibleCount){
	if(eligibleCount==0) eligibleCount=1;
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/regular";
	var OrderData = "exchange=NSE&tradingsymbol="+whichTab+"&transaction_type=BUY&order_type=LIMIT&quantity="+eligibleCount+"&price=1&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price=0&squareoff=0&stoploss=0&trailing_stoploss=0&variety=regular";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
}
function populateTradeObjects(eligibleSymbolsCount){
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose;
	var whatPrice = 'ltp', whichCandle='1', duration = 3, total = parseInt(0);
	eligibleSymbols.forEach(a=> total+=a.Count);
	var amntToInvestPerSymbol=0;
	if(!isMultipleTab){
		eligibleSymbolsCount = eligibleSymbols.length;
	}
	amntToInvestPerSymbol = invest/eligibleSymbolsCount;
	
	var eligibleSymbolsCountTemp=eligibleSymbolsCount;
	var isPlacingOtherStretegy = false;
	console.log("Started placing order");
	console.log("------------------------------executing Place Entry Orders");
	for(var i=0; i< eligibleSymbols.length; i++){
		name = eligibleSymbols[i].Id; direction = eligibleSymbols[i].Direction; zerodhaId = eligibleSymbols[i].ZerodhaId, count = eligibleSymbols[i].Count, intradayOpen = eligibleSymbols[i].IntradayOpen;
		var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
		if(liveDataFull.ltp==undefined || liveDataFull.ltp==null){
			eligibleSymbolsCount--;
			amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			continue;
		}
		if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 1000 
				|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 1000 ){
			eligibleSymbolsCount--;
			amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			zeroQtySymbols[zeroQtySymbols.length] = i;
			continue;
		}
		price = liveDataFull.ltp;
		if(direction=='SELL')
		{
			entryPrice = price - (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "SELL", typeOfOrder, boVarietyType, count, intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==true){
				eligibleSymbolsCount--;
				amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			}
		}
		else if(direction =='BUY'){
			entryPrice = price + (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "BUY", typeOfOrder, boVarietyType, count,intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==true){
				eligibleSymbolsCount--;
				amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			}
		}
	}
	amntToInvestPerSymbol = invest/eligibleSymbolsCountTemp;
	
	//rerunZeroQtySymbols(amntToInvestPerSymbol);
	//placeOtherStrategy();
	placeStopLossOrders();
}

function placeOtherStrategy(){
	var isRejected=false, amntToInvestPerSymbol=0;
	var isPlacingOtherStretegy = true;
	console.log("------------------------------executing placeOtherStrategy");
	for (var i=0; i< otherStrategyObjects.length; i++){
		if(otherStrategyObjects[i].TransactionType=='SELL'){
			var intradayOpen = otherStrategyObjects[i].LimitPrice;
			var trig = getTickPrice(parseFloat(intradayOpen + (intradayOpen*availableAtCheaperPricePercFromOpen/100)));
			otherStrategyObjects[i].LimitPrice = trig;
			amntToInvestPerSymbol = otherStrategyObjects[i].Quantity * otherStrategyObjects[i].LimitPrice;
			isRejected = placeZerodhaOrder(otherStrategyObjects[i], amntToInvestPerSymbol, isPlacingOtherStretegy);
		}
		else if(otherStrategyObjects[i].TransactionType=='BUY'){
			var intradayOpen = otherStrategyObjects[i].LimitPrice;
			var trig = getTickPrice(parseFloat(intradayOpen - (intradayOpen*availableAtCheaperPricePercFromOpen/100)));
			otherStrategyObjects[i].LimitPrice = trig;
			amntToInvestPerSymbol = otherStrategyObjects[i].Quantity * otherStrategyObjects[i].LimitPrice;
			isRejected = placeZerodhaOrder(otherStrategyObjects[i], amntToInvestPerSymbol, isPlacingOtherStretegy);
		}
	}
}

function rerunZeroQtySymbols(amntToInvestPerSymbol){
	var isPlacingOtherStretegy = false;
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose;
	var whatPrice = 'ltp', whichCandle='1', duration = 3, total = parseInt(0);
	console.log("------------------------------executing rerunZeroQtySymbols");
	for(var i=0; i< zeroQtySymbols.length; i++){
		name = eligibleSymbols[zeroQtySymbols[i]].Id; direction = eligibleSymbols[zeroQtySymbols[i]].Direction; zerodhaId = eligibleSymbols[zeroQtySymbols[i]].ZerodhaId, count = eligibleSymbols[zeroQtySymbols[i]].Count, intradayOpen = eligibleSymbols[zeroQtySymbols[i]].IntradayOpen;
		var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
		if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 5000 
				|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 5000 ){
			
			continue;
		}
		price = liveDataFull.ltp;
		if(direction=='SELL')
		{
			entryPrice = price - (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "SELL", typeOfOrder, boVarietyType, count,intradayOpen,squareOff, stopLoss);
			placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
		}
		else if(direction =='BUY'){
			entryPrice = price + (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "BUY", typeOfOrder, boVarietyType, count, intradayOpen,squareOff, stopLoss);
			placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
		}
	}
}
function placeZerodhaOrder(entryTradeObjects, amntToInvestPerSymbol, isPlacingOtherStretegy){
	var isRejected=false;
	console.log(entryTradeObjects);
	console.log(amntToInvestPerSymbol);
	if(isPlaceOrder){
		var orderId = PlaceOrder(entryTradeObjects, amntToInvestPerSymbol);
		isRejected = isOrderRejected(orderId);
		//!isRejected && !isPlacingOtherStretegy
		if(!isRejected && !isPlacingOtherStretegy)
		{
			var noOfShares = amntToInvestPerSymbol/entryTradeObjects.LimitPrice;
			if(noOfShares<=1){
				noOfShares=1;
			}
			entryTradeObjects.Quantity = parseInt(noOfShares);
			otherStrategyObjects[otherStrategyObjects.length] = entryTradeObjects;
		}
	}
	return isRejected;
}

function getPositions(){
	var url="https://kite.zerodha.com/api/portfolio/positions";
	var xhr = new XMLHttpRequest();
	xhr.open("GET", url, false);
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.send();
	return JSON.parse(xhr.response);
}

function placeStopLossOrders()
{
	console.log("started placing stoploss orders");
	var res = getPositions();
	for(var i=0; i<res.data.net.length; i++){
		var obj = res.data.net[i];
		var dir="", quantity=0, last_price=0;
		if(parseInt(obj.quantity)!=0)
		{
			if(parseInt(obj.buy_quantity) > parseInt(obj.sell_quantity)){
				dir="SELL";
				quantity = parseInt(obj.buy_quantity)-parseInt(obj.sell_quantity);
				last_price = obj.average_price-(obj.average_price*stopLossPerc)/100;
				last_price = getTickPrice(last_price);
			}else if(parseInt(obj.sell_quantity) > parseInt(obj.buy_quantity)){
				dir="BUY";
				quantity = parseInt(obj.sell_quantity)-parseInt(obj.buy_quantity);
				last_price = obj.average_price+(obj.average_price*stopLossPerc)/100;
				last_price = getTickPrice(last_price);
			}
			obj.tradingsymbol = obj.tradingsymbol.replace("&", "%26");
			
			var exitOrder = generateOrderObject(obj.tradingsymbol, last_price, dir, typeOfOrder, varietyType, quantity,0,0,0);
			exitOrder.Quantity=quantity;

			PlaceStopLossOrder(exitOrder, varietyType);
		}
	}
}

function PlaceStopLossOrder(Order, variety)
{
	Order.TriggerPrice=Order.LimitPrice;
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/"+varietyType;
	var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type=SL-M&quantity="+Order.Quantity+"&price=0&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff=0&stoploss=0&trailing_stoploss=0&variety="+variety+"&user_id=DP3137";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
}

function isOrderRejected(orderId){
	var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send();
	var res = JSON.parse(xhr.response);
	var index = res.data.findIndex(obj=>obj.order_id == orderId);
	if(res.data[index].status == 'REJECTED'){
		return true;
	}else{
		return false;
	}
}

function getEligibleSymbolCountAcrossTabs(){
	var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send();
	var res = JSON.parse(xhr.response);
	var totalCount = 0;
	if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_1')<0){
		getEligibleSymbolCountAcrossTabs();
	}
	if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_2')<0){
		getEligibleSymbolCountAcrossTabs();
	}
	if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_3')<0){
		getEligibleSymbolCountAcrossTabs();
	}
	if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_4')<0){
		getEligibleSymbolCountAcrossTabs();
	}
	for(var i=0; i< res.data.length; i++){
		if(res.data[i].tradingsymbol == 'tab_1'){
			totalCount += res.data[i].quantity;
		}else if(res.data[i].tradingsymbol == 'tab_2'){
			totalCount += res.data[i].quantity;
		}else if(res.data[i].tradingsymbol == 'tab_3'){
			totalCount += res.data[i].quantity;
		}else if(res.data[i].tradingsymbol == 'tab_4'){
			totalCount += res.data[i].quantity;
		}
	}
	return totalCount;
}

function startEntry(){
	console.log(new Date());
	getEligibleSymbols();
	console.log("Eligible symbols collected");
	console.log(eligibleSymbols);
	var eligibleSymbolsCount=0;
	if(isMultipleTab){
		executeDummyOrder(whichTab, eligibleSymbols.length);
		eligibleSymbolsCount = getEligibleSymbolCountAcrossTabs();
	}
	 
	if(eligibleSymbols.length>0){
		console.log("Across tabs eligible symbols count="+eligibleSymbolsCount);
		console.log("This tab eligible symbols count="+eligibleSymbols.length);
		populateTradeObjects(eligibleSymbolsCount);
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

function PlaceOrder(Order, amntToInvestPerSymbol)
{ 
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/"+Order.Variety;
	noOfShares = amntToInvestPerSymbol/Order.LimitPrice;
	if(noOfShares<=1){
		noOfShares=1;
	}
	Order.Quantity = parseInt(noOfShares);
	var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type="+Order.OrderType+"&quantity="+Order.Quantity+"&price="+Order.LimitPrice+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff="+Order.SquareOff+"&stoploss="+Order.StopLoss+"&trailing_stoploss=0&variety="+Order.Variety+"";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
	
	var res = JSON.parse(xhr.response);
	return res.data.order_id;
}

function getPriceFromUpstox(exchange, name, type){
	var xhr = new XMLHttpRequest();
	xhr.open("GET", 'https://api.upstox.com/live/feed/now/'+exchange+'/'+name+'/'+type+'', false);
	xhr.setRequestHeader("authorization", "Bearer "+upstoxAccessToken);
	xhr.setRequestHeader("x-api-key", upstoxApiKey);
	xhr.send();
	var res = JSON.parse(xhr.response);
	var liveDataFull={};
	if(res.data != null && res.data != undefined){
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
	now = new Date();
	millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, parseInt(minute), second, 0) - now;
	if (millisTill10 < 0) {
		millisTill10 += 86400000; 
		console.log("it's already after time");
	}
	setTimeout(startEntry, millisTill10);
}

start();

